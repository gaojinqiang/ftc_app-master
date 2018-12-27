package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Separate utility class to handle omnidirectional motion on mecanum wheels.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class DriveHandler {
    private static final MotorPowerSet ZERO = new MotorPowerSet(0, 0, 0, 0);
    private static final int DEFAULT_WAIT_TIME = 200;
    private static final Object lock = new Object();
    public static float MOVE_MULT = 4450f; //change to tweak "move x meters" precisely. Degrees wheel turn per unit.
    public static float TURN_MULT = 1205f; //change to tweak "rotate x deg" precisely.   Degrees wheel turn per
    //we have a separate thread handling moveTasks. This is so the robot can still do other stuff
    //while this is happening at the same time.
    private static MoveThread moveThread;
    public boolean moveEndFlag = false;
    // radians robot turn
    private Queue<MoveTask> moveTasks; //the currentPos moveTasks to do;
    //NOW THE FUN STUFF, FOR AUTONOMOUS MOTION.
    //the motors
    //[ FL, FR, BL, BR ]
    private DcMotor[] motors;
    //keeping track of location.
    private float curX, curY;

    /**
     * construct by motors
     */
    private DriveHandler(DcMotor leftFront, DcMotor rightFront, DcMotor backLeft, DcMotor backRight) {
        motors = new DcMotor[4];
        motors[0] = leftFront;
        motors[1] = rightFront;
        motors[2] = backLeft;
        motors[3] = backRight;
        moveThread = null;
        moveTasks = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 4; i++) {
            motors[i].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
        startMoveThread();
    }

    /**
     * construct by Ben Bielin Code
     */
    DriveHandler(HardwareTestBot r) {
        this(r.leftFront, r.rightFront, r.leftBack, r.rightBack);
    }

    /**
     * Generates a MotorPowerSet that corresponds to turning the robot in the specified direction
     */
    private static MotorPowerSet calcPowerSet(float direction, float speed, float turnRate) {
        //DO THE MATH
        assert (speed >= 0 && speed <= 1);
        speed = Math.abs(speed);
        if(speed > 1f) {
            speed = 1f;
        }
        float robotAngle = (float) (direction + Math.PI / 4);
        float v1 = (float) (speed * Math.sin(robotAngle) + turnRate);
        float v2 = (float) (speed * Math.cos(robotAngle) - turnRate);
        float v3 = (float) (speed * Math.cos(robotAngle) + turnRate);
        float v4 = (float) (speed * Math.sin(robotAngle) - turnRate);
        //if any level is greater than 1, scale down to prevent robot going off course
        //if turnRate is 0 and speed <1, this wont be a problem.
        float max = Math.max(Math.max(Math.abs(v1), Math.abs(v2)), Math.max(Math.abs(v3), Math.abs(v4)));
        if (max < 1) max = 1;
        return new MotorPowerSet(v1 / max, v2 / max, v3 / max, v4 / max);
    }

    public void resetCoords() {
        curX = curY = 0;
    }

    public void setModeEncoder() {
        for (int i = 0; i < 4; i++) {
            motors[i].setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    /**
     * starts the MoveTasks handling thread. A new thread might be created.
     */
    public void startMoveThread() {
        if (moveThread == null) {
            moveThread = new MoveThread();
            moveThread.setName("MoveThread");
            moveThread.start();
        }
    }

    /**
     * returns if there are any move tasks currently
     */
    public boolean hasTasks() {
        return !moveTasks.isEmpty();
    }

    /**
     * adds a MoveTask to move in a straight line a specified direction and distance.
     */
    public void move(float direction, float speed, float distance) { //speed should be from 0 to 1
        move(direction, speed, distance, DEFAULT_WAIT_TIME);
        waitForDone();
    }

    public void move(float direction, float speed, float distance, int waitTime) { // maybe has some problems here
        direction = (float) Math.toRadians(direction);
        addTask(new MoveTask(calcPowerSet(direction, speed, 0),
                distance * MOVE_MULT / speed, distance,
                direction, waitTime));
    }

    /**
     * ads a move task to rotate in place a specified number of degrees, positive or negative.
     */
    public void turn(float degrees, float speed) {
        turn(degrees, speed, DEFAULT_WAIT_TIME);
    }

    public void turn(float degrees, float speed, int waitTime) {
        degrees = (float) Math.toRadians(degrees);
        addTask(new MoveTask(calcPowerSet(0, 0, speed * Math.signum(degrees)),
                degrees * TURN_MULT / speed,
                0, 0, waitTime));
    }

    private void addTask(MoveTask task) {
        moveTasks.add(task);
        synchronized (lock) {
            lock.notify();
        }
    }

    //measured in given arbitrary units
    public void moveTo(float x, float y, float speed) {
        float dx = curX - x, dy = curY - y;
        float angle = (float) Math.atan2(x, y);
        float distance = (float) Math.hypot(dx, dy);
        move(angle, speed, distance);
    }


    /**
     * cancels all tasks and stops robot.
     */
    public void cancelTasks() {
        moveTasks.clear();
        stopRobot();
    }

    /**
     * set motors to given powerSet.
     */
    private void setPower(MotorPowerSet p) {
        for (int i = 0; i < 4; i++)
            motors[i].setPower(p.power[i]);
    }

    /**
     * Tells the robot to move in a specied direction and turnRate.
     * Can handle motor power levels greater than 1 -- it will scale them down.
     * If you're not turning and moving at the same time, and speed <=1, that wont be a problem
     */
    public void moveAt(float direction, float speed, float turnRate) {
        setPower(calcPowerSet(direction, speed, turnRate));
    }

    /**
     * Stops robot, i.e., set motor power levels to zero.
     */
    public void stopRobot() {
        setPower(ZERO);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void waitForDone() {
        while (hasTasks()) ;
    }

    /**
     * a set of power levels for algit rm --cached -r .ideal 4 motors;
     * just a container around float[][]
     */
    public static class MotorPowerSet {
        float[] power;

        MotorPowerSet(float leftFront, float rightFront, float backLeft, float backRight) {
            float[] power = new float[4];
            power[0] = leftFront;
            power[1] = rightFront;
            power[2] = backLeft;
            power[3] = backRight;
            this.power = power;
        }

        MotorPowerSet() {
            this.power = new float[4];
        }
    }

    private class MoveTask { //NOT STATIC, to access DCmotors
        //dont want to spam new, so I have fields instead of vars.
        private MotorPowerSet targetPower, actualPower;
        private float multiplier;
        private float[] progress;
        private int waitTime;
        private float dx, dy;

        MoveTask(MotorPowerSet targetPower, float multiplier, float distance, float angle, int waitTime) {
            this.targetPower = targetPower;
            this.multiplier = multiplier;
            this.waitTime = waitTime;
            this.dx = (float) (distance * Math.sin(angle));
            this.dy = (float) (distance * Math.cos(angle));
            this.actualPower = new MotorPowerSet();
            progress = new float[4];
        }

        void start() {
            for (int i = 0; i < 4; i++) {
                motors[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                motors[i].setMode(DcMotor.RunMode.RUN_TO_POSITION);
                motors[i].setTargetPosition((int) (multiplier * targetPower.power[i]));
            }
            setPower(targetPower);
        }

        //read motor positions and adjust them as necessary if they go off track.
        //supposed to make all the motors turn in unison.
        boolean process() {
            float avgProgress = 0;
            for (int i = 0; i < 4; i++) {
                progress[i] = (float) motors[i].getCurrentPosition() / motors[i].getTargetPosition();
                if (Float.isNaN(progress[i])) progress[i] = 1;
                avgProgress += progress[i];
            }
            avgProgress /= 4;
            //adjust power as necessary..
            for (int i = 0; i < 4; i++) {
                actualPower.power[i] = Math.abs(targetPower.power[i] * (1 - 3 * (progress[i] - avgProgress)));
            }
            setPower(actualPower);
            return Math.abs(avgProgress - 1) < 0.03;
        }

    }

    private class MoveThread extends Thread {
        private boolean isFirstTime;
        private boolean exitFlag;

        MoveThread() {
            exitFlag = false;
        }

        //continually run moveTasks;
        @Override
        public void run() {
            isFirstTime = true;
            while (true) {
                if (exitFlag) return;
                try {
                    synchronized (lock) {
                        while (moveTasks.isEmpty()) {
                            lock.wait();
                        }
                    }
                    MoveTask curTask = moveTasks.element();
                    if (exitFlag) return;
                    if (isFirstTime) {
                        isFirstTime = false;
                        curTask.start();
                    }
                    if (exitFlag) return;
                    if (curTask.process()) {
                        stopRobot();
                        int waitTime = curTask.waitTime;
                        curX += curTask.dx;
                        curY += curTask.dy;
                        moveTasks.remove();
                        isFirstTime = true;
                        moveEndFlag = true;
                        Thread.sleep(waitTime);
                    }
                } catch (NullPointerException | InterruptedException ignored) {
                    return;
                }
            }
        }
    }
}
