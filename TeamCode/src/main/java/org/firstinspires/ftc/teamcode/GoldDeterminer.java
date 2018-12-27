package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class GoldDeterminer {
    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    @SuppressWarnings({"SpellCheckingInspection", "LongLine"})
    private static final String VUFORIA_KEY = "Aavay7//////AAABmS26wV70nE/XoqC91tMM/rlwbqInv/YUads4QRll085q/yT" +
            "+qW0qdyrUwXPXbvwDkGhnffFMGIizzvfrXviNCbfAAgJzSwDJuL0MJl3LRE2FU4JMKKU2v7V+XGChhH91BXriKEtx4PDCq5DwSpCT1TP3XSJrouflaIEdqxTcUz/LaIEh4phJs35awBUu+g+4i3EKMJBsYWyJ0V9jdI5DLCVhXkKtBpKgJbO3XFx40Ig/HFXES1iUaOk2fj9SG/jRUsWLH1cs35/g289Xs6BTQTHnGpX9bcOvK0m4NkhogjqbT7S76O91jeheUZwazesROu848shb317YhWIclBSR/vV9/I2fT+485YdwnaxuS8K9";
    private static final int MAX_DIFF = 40;
    private static ByLeft byLeft = new ByLeft();
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;
    private boolean detected = true;
    private int goldPos = -1;

    public GoldDeterminer(HardwareMap hardwareMap) {
        initVuforia();
        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod(hardwareMap);
        } else {
            throw new UnsupportedOperationException("This device is not compatibles with TFOD");
        }
    }

    //Start the gold recognitions thread.
    public void start() {
        if (!detected) return;
        detected = false;
        goldPos = -1;
        DetectorThread detectorThread = new DetectorThread();
        detectorThread.start();
    }

    public boolean hasDetected() {
        return detected;
    }

    public int goldPosition() {
        if (!detected) return -1;
        return goldPos;
    }

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CameraDirection.BACK;
        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    /**
     * Initialize the Tensor Flow Object Detection engine.
     */
    private void initTfod(HardwareMap hardwareMap) {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }

    private static class ByLeft implements Comparator<Recognition> {
        @Override
        public int compare(Recognition lhs, Recognition rhs) {
            return (int) (lhs.getLeft() - rhs.getLeft());
        }
    }

    private class DetectorThread extends Thread {

        private final Recognition[] chainedRecognitions = new Recognition[3];

        @Override
        public void run() {
            tfod.activate();
            int consecutive = 0;
            //break;
            while (!detected) {
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (getChainedRecognitions(updatedRecognitions)) {
                    //now we have three horizontally level recognitions
                    int curGoldPos = findGoldPos(); //find gold position.
                    if (curGoldPos == -1) continue; //gold position could not be determined.
                    if (goldPos == -1) goldPos = curGoldPos;
                    if (goldPos == curGoldPos) { //if matches previous recognition, increase consecutive counter.
                        consecutive++;
                    } else { //otherwise, reset counter
                        goldPos = curGoldPos;
                        consecutive = 0;
                    }
                }
                // if the goldPos is NOT determined, no need to reset consecutive.
                // we only care about possible mis-recognitions.
                if (consecutive == 3) //if 3 in a row, then can determine with confidence.
                    detected = true;
            }
            tfod.shutdown();
        }

        private int findGoldPos() {
            int theGoldOne = -1;
            for (int i = 0; i < 3; i++) {
                if (chainedRecognitions[i].getLabel().equals(LABEL_GOLD_MINERAL)) {
                    if (theGoldOne == -1) {
                        theGoldOne = i;
                    } else {//if there is more than one gold one, is indeterminate.
                        theGoldOne = -1;
                        break;
                    }
                }
            }
            if (theGoldOne == -1) return -1;
            int curGoldPos = -1;
            //How many the gold one is further left to, is also its position.
            for (int i = 0; i < 3; i++) {
                if (chainedRecognitions[theGoldOne].getBottom() >= chainedRecognitions[i].getBottom())
                    curGoldPos++;
            }
            return curGoldPos;
        }

        //find 3 horizontally chained recognitions, returns true if successful
        private boolean getChainedRecognitions(List<Recognition> recognitionsList) {
            if (recognitionsList == null || recognitionsList.size() < 3) return false;
            int numValid = 0;
            float prevLeft = 0;
            Recognition[] recognitions = new Recognition[recognitionsList.size()];
            recognitionsList.toArray(recognitions);
            //sort, actually by the bottom (orientation issues?). The bottommost will be processed first.
            Arrays.sort(recognitions, byLeft);
            //if any recognitions overlap, prioritize the silver one (delete the gold one).gf`
            // since the square on floor is somehow recognized as a gold.
            for (int i = 0; i < Math.min(recognitions.length, 6); i++) {
                if (recognitions[i] == null) continue;
                //if it overlaps closely with other recognitions and is silver, override it with silver.
                for (int j = i + 1; j < Math.min(recognitions.length, 6); j++) {
                    if (recognitions[j] == null) continue;
                    if (Math.hypot(recognitions[j].getTop() - recognitions[i].getTop(),
                            recognitions[j].getBottom() - recognitions[i].getBottom()) <
                            Math.max(recognitions[i].getHeight(), recognitions[j].getHeight())) {
                        boolean jGold = recognitions[j].getLabel().equals(LABEL_GOLD_MINERAL);
                        if (jGold) recognitions[j] = null;
                        else recognitions[i] = null;
                        break;
                    }
                }
            }
            //process (remaining) recognitions.
            for (Recognition curRecognition : recognitions) {
                if (curRecognition == null) continue;
                //restart chain if a recognition is too far off from the previous.
                //remember, sorted already sorted by Left first.
                if (numValid != 0 && Math.abs(curRecognition.getLeft() - prevLeft) > MAX_DIFF) {
                    numValid = 0;
                    continue;
                }
                prevLeft = curRecognition.getLeft();
                chainedRecognitions[numValid] = curRecognition;
                numValid++;
                if (numValid == 3) return true;
            }
            return false;
        }
    }
}
