package com.chsrobotics.ftccore.engine.localization.localizer;

import com.chsrobotics.ftccore.geometry.Position;
import com.chsrobotics.ftccore.hardware.HardwareManager;
import com.chsrobotics.ftccore.management.constants.MiscConstants;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;

public class OdometryLocalizer extends Localizer{

    /*
    Encoder localization is different from DeadWheelLocalizer. Instead of using odometry pods, encoder localization utilizes
    the motor encoders on the four drive motors that are used to drive the 4 wheels on the robot. While this is slightly less
    accurate than pods, this method is still good for teams that do not have pods.
    */

    HardwareManager hardware;

    private int lastLatPos, lastLongPos;
    private double lastTheta;

    public OdometryLocalizer(Position initialState, HardwareManager hardware) {
        super(initialState, hardware);
        this.hardware = hardware;
    }


    public double getThetaDifference(double current, double old) {
        double thetaError = current - old;
        boolean isCounterClockwise = false;

        if (Math.abs(current - (old - (2 * Math.PI))) < Math.abs(thetaError))
        {
            thetaError = current - (old - (2 * Math.PI));
            isCounterClockwise = true;
        }

        if (Math.abs(current - (old + (2 * Math.PI))) < thetaError)
        {
            thetaError = current - (old + (2 * Math.PI));
            isCounterClockwise = true;
        }

        if (thetaError > 0 && (thetaError < Math.PI)) {
            isCounterClockwise = true;
        }

        if (thetaError < 0 && (thetaError > -Math.PI)) {
            isCounterClockwise = false;
        }

        return Math.abs(thetaError) * (isCounterClockwise ? 1 : -1);
    }

    @Override
    public Position getRobotPosition(Position previousPosition) {
        //Encoder values. These are in ticks. We will later convert this to a usable distance.

        //Record encoder values.
        int lateralPos = hardware.accessoryOdometryPods[0].getCurrentPosition();
        int longitudinalPos = hardware.accessoryOdometryPods[1].getCurrentPosition();

        double theta = hardware.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle - hardware.offset;
        if (theta > 2 * Math.PI) {
            theta -= 2 * Math.PI;
        } else if (theta < 0) {
            theta += 2 * Math.PI;
        }

        //Displacement values

        //Calculate displacement values
        double latDisp = (lateralPos - lastLatPos) * 0.05368932757;
        double longDisp = (longitudinalPos - lastLongPos) * 0.05368932757;

        //Store encoder values so we can use them in calculating displacement.
        lastLatPos = lateralPos;
        lastLongPos = longitudinalPos;

        double deltaXf = latDisp * Math.cos(theta) - latDisp * Math.sin(theta);
        double deltaYf = longDisp * Math.cos(theta) + longDisp * Math.sin(theta);

        Position robotPosition = new Position();
        robotPosition.x = previousPosition.x - deltaXf;
        robotPosition.y = previousPosition.y - deltaYf;
        robotPosition.t = theta;

        if (robotPosition.x == 0) robotPosition.x = 0.0000001;

        return robotPosition;

        //Calculate net displacement after accounting for rotation

        //Holonomic displacement in robot reference frame.
//        double deltaX, deltaY;

        //Compute displacement in robot reference frame.
//        deltaX = (robotLfDisp + robotRfDisp - robotRbDisp - robotLbDisp) / (2 * Math.sqrt(2));
//        deltaY = (robotLfDisp - robotRfDisp - robotRbDisp + robotLbDisp) / (2 * Math.sqrt(2));

        //Robot theta
/*        double theta;

        //Compute robot theta
        theta = hardware.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle - hardware.offset;
        if (theta > 2 * Math.PI) {
            theta -= 2 * Math.PI;
        } else if (theta < 0) {
            theta += 2 * Math.PI;
        }

        //Compute displacement in field reference frame.
        double deltaXf = deltaX * Math.cos(theta) - deltaY * Math.sin(theta);
        double deltaYf = deltaY * Math.cos(theta) + deltaX * Math.sin(theta);

        Position robotPosition = new Position();
        robotPosition.x = previousPosition.x - deltaXf;
        robotPosition.y = previousPosition.y - deltaYf;
        robotPosition.t = theta;

        if (robotPosition.x == 0) robotPosition.x = 0.0000001;

        return robotPosition;*/
    }

    @Override
    public void updateRobotPosition(Position pos) {

    }
}
