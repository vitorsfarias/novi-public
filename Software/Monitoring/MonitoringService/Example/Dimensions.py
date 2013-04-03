'''
Created on Oct 12, 2011

@author: steger
@summary: Here we declare some unit models to enable parameter conversions  
'''
from DataProcessing.Dimension import DimensionManager
from Example.Units import UM
from DataProcessing.MeasurementLevel import Nominal, Interval, Ratio

DM = DimensionManager(unitmanager = UM)

basedimensions = [
    ("Cardinal", "unitless", Nominal),
    ("NameOfSomething", "unitless", Nominal),
    ("Countable", "piece", Ratio),
    ("InformationSize", "bit", Ratio),
    ("IPAddress", "ipv4dotted", Nominal),
    ("PointInTime", "unixtimestamp", Interval),
    ]

deriveddimensions = [
    ("TimeInterval", "second", "PointInTime", DM.DifferenceDimension),
    ("Probability", "fraction", "Countable", DM.RatioDimension),
    ]

for reference, unitreference, measurementlevel in basedimensions:
    DM.newBaseDimension(reference, reference, UM[unitreference], measurementlevel)

for reference, unitreference, ancestorreference, dimtype in deriveddimensions:
    DM.newDerivedDimension(reference, reference, UM[unitreference], DM[ancestorreference], dimtype)


#Some dimensions explicitely references
nameofsomething = DM["NameOfSomething"]
pointintime = DM["PointInTime"]
timeinterval = DM["TimeInterval"]
cardinal = DM["Cardinal"]
countable = DM["Countable"]
ipaddress = DM["IPAddress"]
informationsize = DM["InformationSize"]
probability = DM["Probability"]
