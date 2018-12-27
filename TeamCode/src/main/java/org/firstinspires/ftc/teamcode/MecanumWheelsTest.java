package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "MecanumWheelsTest", group = "Test")
public class MecanumWheelsTest extends OpMode {
    HardwareTestBot robot = new HardwareTestBot();
    //boolean a = false;

    @Override
    public void init() {
        robot.init(hardwareMap);
        robot.drive.setModeEncoder();
    }

    @Override
    public void loop() {
        //replaced with drive Handler.
        float angle = (float) Math.atan2(gamepad1.left_stick_x, -gamepad1.left_stick_y); //Up is 0, positive is clockwise
        float speed = (float) Math.hypot(gamepad1.left_stick_x, gamepad1.left_stick_y)*1.4f; //allow going a bit faster, if possible.
        float turnRate = gamepad1.right_stick_x * 2; //prioritize turning over moving.
        //telemetry.addData("Stick angle:",angle);
        robot.drive.moveAt(angle, speed, turnRate);

		/*
		if (gamepad1.right_bumper) {
			a = true;
		}


		while (a) {
			robot.Hooke.setMode(DcMotor.RunMode.RUN_TO_POSITION);
			robot.Hooke.setTargetPosition(35500);
			robot.Hooke.setPower(1);
			if (robot.Hooke.getCurrentPosition() > 35500 || gamepad1.x) {
				robot.Hooke.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
				robot.Hooke.setPower(0);
				a = false;
				break;
			}
			telemetry.addData("current position", robot.Hooke.getCurrentPosition());
			telemetry.update();
		}
*/

        /*
        if(gamepad2.dpad_up) {robot.Arm.setPower(1);}
        else if(gamepad2.dpad_down) {robot.Arm.setPower(-1);}
        else {robot.Arm.setPower(0);}
        */


        if(gamepad2.a) {
            robot.Collection.setPower(1);
        } else if(gamepad2.b) {
            robot.Collection.setPower(-1);
        } else {
            robot.Collection.setPower(0);
        }



    }
}
