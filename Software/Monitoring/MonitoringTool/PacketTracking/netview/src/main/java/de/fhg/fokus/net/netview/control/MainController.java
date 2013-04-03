/**
*
* Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
* Copyright according to BSD License
* For full text of the license see: ./novi/Software/Monitoring/MonitoringTool/PacketTracking/license.txt
*
* @author <a href="mailto:ramon.masek@fokus.fraunhofer.de">Ramon Masek</a>, Fraunhofer FOKUS
* @author <a href="mailto:c.henke@tu-berlin.de">Christian Henke</a>, Technical University Berlin
* @author <a href="mailto:carsten.schmoll@fokus.fraunhofer.de">Carsten Schmoll</a>, Fraunhofer FOKUS
* @author <a href="mailto:Julian.Vetter@campus.tu-berlin.de">Julian Vetter</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Jens Krenzin</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Michael Gehring</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Tacio Grespan Santos</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Fabian Wolff</a>, Fraunhofer FOKUS
*
*/

/* Netview - a software component to visualize packet tracks, hop-by-hop delays,
 *           sampling stats and resource consumption. Netview requires the deployment of
 *           distributed probes (impd4e) and a central packet matcher to correlate the
 *           obervations.
 *
 *           The probe can be obtained at http://impd4e.sourceforge.net/downloads.html
 *
 * Copyright (c) 2011
 *
 * Fraunhofer FOKUS
 * www.fokus.fraunhofer.de
 *
 * in cooperation with
 *
 * Technical University Berlin
 * www.av.tu-berlin.de
 *
 * Ramon Masek <ramon.masek@fokus.fraunhofer.de>
 * Christian Henke <c.henke@tu-berlin.de>
 * Carsten Schmoll <carsten.schmoll@fokus.fraunhofer.de>
 * Julian Vetter <julian.vetter@fokus.fraunhofer.de>
 * Jens Krenzin <jens.krenzin@fokus.fraunhofer.de>
 * Michael Gehring <michael.gehring@fokus.fraunhofer.de>
 * Tacio Grespan Santos
 * Fabian Wolff
 *
 * For questions/comments contact packettracking@fokus.fraunhofer.de
 *
 */
package de.fhg.fokus.net.netview.control;

import de.fhg.fokus.net.netview.model.Model;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class of the application.
 *
 * @author FhG-FOKUS NETwork Research
 *
 *
 */
public final class MainController extends SingleFrameApplication {
    //==[ external services / utilities ]===

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    
    private DataSourcesController dataSourcesController;
    private Model model;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    @Override
    protected void initialize(String[] args) {
        logger.debug("=== Initializing NetView === ");
        model = new Model();
    }

 
    @Override
    protected void startup() {

        executor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    initializeControllers();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        });
    }

  
    private void initializeControllers() {
       
        this.dataSourcesController = new DataSourcesController(executor, model);
        this.dataSourcesController.init();

        if (this.dataSourcesController != null) {
            //logger.debug("DSC beim MainController!");
        }

        this.model.setDataSourcesController(this.dataSourcesController);

        if (this.model.getDataSourcesController() != null) {
            //logger.debug("DSC beim Model!");
        }

    }

    public static MainController getApplication() {
        return Application.getInstance(MainController.class);
    }

    public Model getModel() {
        return model;
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {

        launch(MainController.class, args);
    }

    @Override
    protected void shutdown() {
        super.shutdown();
 
        executor.shutdown();
        scheduler.shutdown();


    }
}
