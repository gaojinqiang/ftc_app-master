package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name = "Gold Knocking Tester", group = "Test")
public class GoldKnockingTester extends LinearOpMode {

    private GoldLooker goldLooker;
    private HardwareTestBot robot = new HardwareTestBot();
    private boolean stop = false;
    private boolean found = false;
    //private int pos = -1;

    @Override
    public void runOpMode() throws InterruptedException {
        robot.init(hardwareMap);
        goldLooker = new GoldLooker(hardwareMap);

        waitForStart();
        while (opModeIsActive()) {//why while loop?
            goldLooker.start();
            robot.drive.move(0, 1f, .4f); //move forwards
            robot.drive.move(270, 1f, 17f / 36); //move left
            robot.drive.resetCoords();

            int i;
            int look = -1; // -1 means nothing, 0 means white, 1 means gold
            for (i = -1; i <= 1; i++) {// -1 is left, 0 is center, 1 is right position

                while (look == -1) {
                    look = goldLooker.look();
                    while (look == -1) {
                        look = closerLook(robot, goldLooker);
                    }
                }
                if (look == 1) { //found gold
                    robot.drive.move(0, .5f, .3f); // move forwards to hit gold
                    robot.drive.move(180, .5f, .3f); // move back
                    found = true;
                    break;
                }

                if (i != 1) // has not traverse the 3 positions yet
                    robot.drive.move(90, 1f, 17f / 36);
                look = -1;
            }
            if (!found) {
                telemetry.addData("NOT FOUND", "NOT FOUND");
                telemetry.update();
            }

            //robot.drive.move(90, 1f, (1 - i) * 17f / 36);
            if (i == -1) {
                robot.drive.move(90, 1f, 17f / 36);
            } else if (i == 1) {
                robot.drive.move(270, 1f, 17f / 36);
            }

            robot.drive.move(180, 1f, 0.4f);
            robot.drive.stopRobot();

            if (found) {
                break;
            }
        }
    }

    public static int closerLook(HardwareTestBot robot, GoldLooker goldLooker) {
        int look;
        robot.drive.move(0, 0.3f, 2f / 36);
        look = goldLooker.look();
        if (look != -1) {
            return look;
        }
        robot.drive.move(180, 0.3f, 2f / 36);
        robot.drive.move(270, 0.3f, 2f / 36);
        robot.drive.move(0, 0.3f, 2f / 36);
        look = goldLooker.look();
        if (look != -1) {
            return look;
        }
        robot.drive.move(180, 0.3f, 2f / 36);
        robot.drive.move(90, 0.3f, 4f / 36);
        robot.drive.move(0, 0.3f, 2f / 36);
        look = goldLooker.look();
        return look;
    }
}

