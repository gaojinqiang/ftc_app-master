package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "DriveTweak", group = "Test")
public class DriveHandlerTweak extends OpMode {
    private HardwareTestBot robot = new HardwareTestBot();
    private boolean wait = false;
    private float angle, speed = 1, distance = 1; // = 0

    @Override
    public void init() {
        robot.init(hardwareMap);
        telemetry.addLine("Left stick up/down is distance, right/left is speed");
        telemetry.addLine("Right stick is direction");
        telemetry.addLine("Left/right bumpers to change MOVE_MULT");
        telemetry.addLine("Left/right dpad to change TURN_MULT");
        telemetry.addLine("Press A to start");
        telemetry.update();
    }

    @Override
    public void stop() {
        robot.drive.cancelTasks();
        super.stop();
    }

    private void showNums() {
        telemetry.addData("Distance, arbitrary units: ", distance);
        telemetry.addData("Speed (0-1):", speed);
        telemetry.addData("Direction (deg):", Math.toDegrees(angle));
        telemetry.addData("MOVE_MULT:", DriveHandler.MOVE_MULT);
        telemetry.addData("TURN_MULT:", DriveHandler.TURN_MULT);
        telemetry.addLine("Left stick up/down is distance, right/left is speed");
        telemetry.addLine("Right stick is direction");
        telemetry.addLine("Left/right bumpers to change MOVE_MULT");
        telemetry.addLine("Left/right dpad to change TURN_MULT");
        telemetry.addLine("Press A to start");
    }

    @Override
    public void loop() {
        if (wait) {
            if (gamepad1.x) {
                robot.drive.cancelTasks();
            }
            if (!robot.drive.hasTasks()) {
                wait = false;
                showNums();
                telemetry.update();
            }
            return;
        }
        boolean changed = false;
        if (!robot.drive.hasTasks()) {
            if (Math.abs(gamepad1.left_stick_y) > 0.4) {
                distance += -(Math.abs(gamepad1.left_stick_y) - 0.4) * Math.signum(gamepad1.left_stick_y) / 200;
                changed = true;
            }
            if (Math.abs(gamepad1.left_stick_x) > 0.4) {
                speed += (Math.abs(gamepad1.left_stick_x) - 0.4) * Math.signum(gamepad1.left_stick_x) / 200;
                changed = true;
            }
            if (Math.hypot(gamepad1.right_stick_x, gamepad1.right_stick_y) > 0.5) {
                angle = (float) Math.atan2(gamepad1.right_stick_x, -gamepad1.right_stick_y);
                changed = true;
            }
            if (gamepad1.left_bumper) {
                DriveHandler.MOVE_MULT -= 0.5;
                changed = true;
            }
            if (gamepad1.right_bumper) {
                DriveHandler.MOVE_MULT += 0.5;
                changed = true;
            }
            if (gamepad1.dpad_left) {
                DriveHandler.TURN_MULT -= 0.02;
                changed = true;
            }
            if (gamepad1.dpad_right) {
                DriveHandler.TURN_MULT += 0.02;
                changed = true;
            }
            if (changed) {
                showNums();
                telemetry.update();
            }
            //*
            if (gamepad1.a) {
                robot.drive.move(angle, speed, distance);
                wait = true;
            } else if (gamepad1.b) {
                robot.drive.turn((float) Math.toDegrees(angle), speed);
                wait = true;
            }
            //*/
        }
    }
}
