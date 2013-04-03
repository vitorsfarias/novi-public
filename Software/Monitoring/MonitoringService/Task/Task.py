'''
Created on 08.08.2011

@author: steger, jozsef
@organization: ELTE
@contact: steger@complex.elte.hu
'''

from time import time
from threading import Thread, Event, RLock
from DataProcessing.Data import Data, DataHeader
from DataProcessing.Parameter import ParameterList
from Credential.credentialtypes import Credential
import Driver
from DataProcessing.DataSample import DataSample
import logging

class TaskError(Exception):
    pass

'''
@var STRAT_PERIODICAL: sampling strategy, when samples are taken periodically controlled by wait
@var STRAT_ONDEMAND: sampling strategy, retrievehook is run once, then automatically Task is disabled
@var STRAT_FUNCTIONAL: sampling strategy, user provides a call back function to generate the time series to wait between consecutive sampling
'''
STRAT_PERIODICAL, STRAT_ONDEMAND, STRAT_FUNCTIONAL = range(3)


class SubtaskManager(object):
    '''
    @author: Jozsef Steger
    @summary: SubtaskManager class  provides the basic functionalities:
        1. to generate a new Task
        2. to access a Task referenced by the task identifier
        3. to destroy a Task explicit or referenced by the task identifier
    '''
    logger = logging.getLogger("NOVI.MS.STM")

    class Task(object):
        '''
        @author: Jozsef Steger
        @summary: 
        This class represents a simplified control plane of a wide variety of monitoring tools.
        In order to generate monitoring data tools are manipulated via several steps.
        Descendants of the Task class implement these steps.
        In the simplified work flow the following steps are abstracted:
            - prehook: it is run once to initiate the communication channel to the tool and/or to initialize a monitoring task
            - starthook: in case the tool operates in such a manner, this step is run to trigger the start up of a background measurement
            - retrievehook: implements data retrieval from the tool, it has to transform measurement data to the internal data representation
            - stophook: it takes care for stopping any processes running in the background at the tool
            - posthook: provides a means to clean up after a monitoring task is over
        
        Subclasses must be generated dynamically using the generateSubclass() method
        
        @cvar timeout: the time to wait for the background thread to finish
        
        @cvar STATE_SLEEPING: a state, retirevehook does not carry out data collection, monitoring is kept asleep
        @cvar STATE_RUNNING: a state, retrievehook carries out data collection
        
        @note: Only descendants of this class are intended to be instantiated via the static method generateSubclass

        @ivar slock: lock concurrent enable/disable calls
        @ivar cleanup: the function to call back after task is deleted
        @ivar um: the unit manager
        @ivar strategy: indicates how sampling is done, ie. how long to wait between two measurements
        @ivar _wait: for periodical sampling (STRAT_PERIODICAL) this constant is yielded by the generator
        @ivar gen_functional: for user defined sampling (STRAT_FUNCTIONAL) this is the generator to use
        @ivar credential: credential
        @ivar parameters: parameters
        @ivar sliceID: slice identifier the Task belongs to, -1 means unspecified.
        @ivar data: internal representation of monitoring data
        @ivar inRetrievehook: Event indicating current data retrieval
        @ivar dataAdded: Event indicating new data are produced by former runs
        @ivar stopworker: Event indicating that the Task was disabled
        @ivar t: Thread to take care of new measurements
        @ivar _runcount: integer showing how many data retrieval attempt have been made
        @ivar _durationrecord: a list of time stamp and duration pairs showing when data retrievals 
        happened and how long they took
        '''
        timeout = 5
        STATE_SLEEPING, STATE_RUNNING = range(2)
    
        def __init__(self, taskmanager, credential, parameters = None, samplingfunction = None, **kw):
            '''
            @summary: Constructor
            @param taskmanager: a reference to the SubtaskManager, which needs to called back 
            if explicit deletion of the Task happens to keep the list of tasks coherent
            @type taskmanager: SubtaskManager
            @param credential: the necessary authentication information to access and control the tools
            @type credential: Credential
            @param parameters: the parameters to control the tools, ie. measurement details
            @type parameters: ParameterList
            @param samplingfunction: a generator function yielding a number, if the generator provides a
            finite series of numbers, after all items are consumed the Task gets disabled
            @type samplingfunction: callable 
            @keyword kw: parameters, which are passed to the prehook method of the Task
            @raise TaskError: wrong type of credential
            
            
            @note: deleting the strategy property disables the Task and sets the strategy to STRAT_ONDEMAND
            @note: setting the strategy property takes effect only after the Task is (re)enabled
            '''
            self.slock = RLock()
            self.logger = taskmanager.logger
            self.cleanup = taskmanager.removefromtasklist
            self.um = taskmanager.um
            self._strategy = STRAT_PERIODICAL
            self._wait = 1
            self._sliceID = -1
            self.gen_functional = samplingfunction
            if isinstance(credential, Credential):
                self.credential = credential
            else:
                raise TaskError("wrong type of credential")
            if isinstance(parameters, ParameterList):
                self.parameters = parameters
            else:
                self.parameters = ParameterList()
            D = Data(self.um, self.dataheader)
            self.datasource = DataSample(table = D)
            self.data = self.datasource #FIXME: left here for compatibility
            #FIXME: mimic DataSource to look like Data for some methods
            self.data.getTemplate = D.getTemplate
            def saverecord(record):
                D.saveRecord(record)
                self.datasource.process()
            self.data.saveRecord = saverecord
            #FIXME: mimicry over
            self.inRetrievehook = Event()
            self.dataAdded = Event()
            self.stopworker = Event()
            self.stopworker.set()
            try:
                self.prehook(**kw)
            except:
                raise #TaskError()
            self.t = None
            self._runcount = 0
            self._durationrecord = []
            self.driver = None
            self.dataheader = None
            self.logger.debug("+   INIT %s" % self)
            
        def __del__(self):
            print "DEL"
            self.destroy()

        def destroy(self):
            if self.state == self.STATE_RUNNING:
                # let us join the thread if necessary
                self.disable()
            with self.slock:
                self.posthook()
                if self.cleanup:
                    # remove reference in the SubTaskManager
                    self.cleanup(self)
                    self.cleanup = None
                    self.logger.debug("-   DESTROY %s, reference removed" % self)
                else:
                    self.logger.debug("-   DESTROY %s, reference already removed" % self)
    
        def wait(self, wait):
            '''
            @summary: wait until Task is disabled or the provided waiting time is over
            @param wait: requests time to wait for the next sample
            @type wait: float
            '''
            try:
                self.stopworker.wait( max(.1, float(wait)) )
            except:
                self.stopworker.wait( 1 )
        
        @property
        def sliceID(self):
            return self._sliceID
        @sliceID.setter
        def sliceID(self, value):
            if (self._sliceID != -1) and (self._sliceID != value):
                raise TaskError("you can set sliceID property only once")
            self._sliceID = value
        @sliceID.deleter
        def sliceID(self):
            raise TaskError("shan't ever delete this property")
    
        @property
        def runcount(self):
            return self._runcount
        @runcount.setter
        def runcount(self, value):
            raise TaskError("shan't ever set this property")
        @runcount.deleter
        def runcount(self):
            raise TaskError("shan't ever delete this property")
    
        @property
        def duration(self):
            if len(self._durationrecord):
                return self._durationrecord[-1][1]
            else:
                return -1
        
        @property
        def state(self):
            if self.stopworker.isSet():
                return self.STATE_SLEEPING
            else:
                return self.STATE_RUNNING
    
        def state_hrn(self):
            """
            @summary: return the state of the task in human readable format
            @return: the state of the task
            @rtype: string
            """
            if self.stopworker.isSet():
                return "SLEEPING"
            else:
                return "RUNNING"
        
        @property
        def strategy(self):
            with self.slock:
                return self._strategy
        @strategy.setter
        def strategy(self, value):
            if value in [STRAT_ONDEMAND, STRAT_PERIODICAL, STRAT_FUNCTIONAL]:
                with self.slock:
                    self._strategy = value
        @strategy.deleter
        def strategy(self):
            self.disable()
            with self.slock:
                self._strategy = STRAT_ONDEMAND
        
        def gen_ondemand(self):
            yield 0
        
        def gen_periodic(self):
            while True:
                yield self._wait
        
        def enable(self):
            """
            @summary: enable task
            """
            with self.slock:
                if self.t is not None:
                    raise TaskError("You can enable a perfectly disabled Task")
                self.stopworker.clear()
                # run starthook
                self.starthook()
                # initialize working thread
                self.t = Thread(target = self._worker, name = str(self))
                self.t.setDaemon(True)
                self.t.start()

        def disable(self):
            """
            @summary: disable task
            """
            with self.slock:
                if self.t is None:
                    self.logger.debug("W   %s already disabled" % self)
                    return
                self.stopworker.set()
                try:
                    # wait for working thread to finish
                    n = 0
                    while True:
                        n += 1
                        self.t.join(self.timeout)
                        if self.t.isAlive():
                            self.logger.error("E   %s timeout occurred %d times while disabling task" % self)
                        else:
                            break
                except RuntimeError:
                    # generator does not provide any more waiting time interval
                    # thread tries to join itself
                    pass
                # run stophook
                try:
                    self.stophook()
                finally:
                    self.t = None
            
        def _worker(self):
            '''
            @summary: This method is running in a background thread.
            It takes care of calling retrievehook. 
            If new data are produced by the tool it is indicated via dataAdded.
            '''
            strategy = self.strategy
            if strategy == STRAT_ONDEMAND:
                generator = self.gen_ondemand
            elif strategy == STRAT_PERIODICAL:
                generator = self.gen_periodic
            elif strategy == STRAT_FUNCTIONAL:
                generator = self.gen_functional
            for wait in generator():
                if not self.stopworker.isSet():
                    # the task is still running
                    self._runcount += 1
                    self.inRetrievehook.set()
                    invocation = time()
                    try:
                        R = self.retrievehook()
                    except Exception, e:
                        self.logger.error("E   %s exception in retrievehook() %s" % (self, e))
                        break
                    finally:
                        self.inRetrievehook.clear()
                    stopped = time()
                    self._durationrecord.append( (invocation, stopped - invocation) )
                    if R:
                        self.dataAdded.set()
                else:
                    # the task is disabled
                    break
                self.wait(wait)
            if not self.stopworker.isSet():
                # the Task is not disabled
                # but there are no more items in the series of waiting time
                # so we disable it
                self.disable()

    def __init__(self, unitmanager):
        self._tasks = {}
        self.tasklock = RLock()
        self._lastid = -1
        self.um = unitmanager
        self.logger.debug("II: INIT %s" % self)

    def __del__(self):
        self.logger.debug("II: DEL %s" % self)

    def __str__(self):
        return "<SubtaskManager %s (%d tasks)>" % (id(self), len(self))
    
    def __len__(self):
        return len(self._tasks)
        
    @property
    def uniqid(self):
        self._lastid += 1
        return self._lastid

    @staticmethod
    def _shiftMethod(implementation):
        '''
        @summary: helps indentation of the piece of implemented code
        '''
        return "\n\t\t".join( filter(None, implementation.splitlines())  )

    def generate(self, name, driver, dataheader, hookimplementations, credential, parameters, samplingfunction = None, **kw):
        '''
        @summary: This method is responsible for dynamically generate a new Task subclass
        @param name: the name of the dynamically generated class
        @type name: string
        @param driver: driver implementing a communication channel to the tool
        @type driver: Driver
        @param dataheader: contains information of the data structure of the result
        @type dataheader: DataHeader
        @param hookimplementations: the work flow steps (hooks)  
        @type hookimplementations: dict
        @param credential: the credential used by the driver
        @type credential: dict
        @param parameters: list of control parameters to fine tune measurements
        @type parameters: ParameterList
        @param samplingfunction: a generator yielding the time interval elements of a series to wait between consecutive samples
        @type samplingfunction: callable
        @keyword kw: extra keyword arguments passed to the prehook of the new Task
        @return: identifier and the subclass instance of (Task)
        @rtype: int, Task
        @raise TaskError: wrong Driver type / wrong DataHeader type / erroneous implementation 
        '''
        prehook = self._shiftMethod( hookimplementations.get("prehook", "pass") )
        starthook = self._shiftMethod( hookimplementations.get("starthook", "pass") )
        stophook = self._shiftMethod( hookimplementations.get("stophook", "pass") )
        posthook = self._shiftMethod( hookimplementations.get("posthook", "pass") )
        retrievehook = self._shiftMethod( hookimplementations.get("retrievehook", "raise TaskException(\"retrievehook() must be implemented\")") )
        if not issubclass(driver, Driver.Driver.Driver):
            raise TaskError("wrong Driver type %s" % driver)
        if not isinstance(dataheader, DataHeader):
            raise TaskError("wrong DataHeader type %s" % dataheader)
        classtemplate = """
import re
from DataProcessing.Data import DataHeader
class %s(SubtaskManager.Task):
\tdef prehook(self, **kw):
\t\t%s
\tdef starthook(self):
\t\t%s
\tdef retrievehook(self):
\t\t%s
\tdef stophook(self):
\t\t%s
\tdef posthook(self):
\t\t%s""" % (name, prehook, starthook, retrievehook, stophook, posthook)
        try:
            exec(classtemplate, globals())
        except:
            self.logger.error(classtemplate)
            raise TaskError("erroneous implementation (%s)" % name)
        taskclass = globals()[name]
        taskclass.driver = driver
        taskclass.dataheader = dataheader
        taskid = self.uniqid
        task = taskclass(self, credential, parameters, samplingfunction, **kw)
        with self.tasklock:
            self._tasks[taskid] = task
        self.logger.info("++: %s new Task %s is identified by %d" % (self, task, taskid))
        return taskid, task
    
    def __getitem__(self, taskidentifier):
        taskidentifier = int(taskidentifier)
        with self.tasklock:
            return self._tasks[taskidentifier]
            
    def getidentifier(self, task):
        with self.tasklock:
            for tid, t in self._tasks.iteritems():
                if task == t:
                    return tid
        raise TaskError("Task %s is unknown to me" % task)
    
    def removefromtasklist(self, task):
        with self.tasklock:
            try:
                taskidentifier = self.getidentifier(task)
                self.pop(taskidentifier)
            except TaskError:
                pass
    
    def pop(self, taskidentifier):
        try:
            task = self._tasks.pop(taskidentifier)
            task.cleanup = None
            self.logger.info("--: %s Task %s identified by %d removed" % (self, task, taskidentifier))
            task.destroy()
            task = None
        except KeyError:
            self.logger.warning("WW: %s Task to remove identified by %s is unknown to me" % (self, taskidentifier))

    def tasks_of_slice(self, sliceID = -1):
        for t in self._tasks.values():
            if t.sliceID == sliceID:
                yield t
