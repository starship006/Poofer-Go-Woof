package PooferOne;

import battlecode.common.*;



//MAKE SURE YOU RUN CANMOVE BEFORE CALLING THESE MOVEMENT OPTIONS
public class MovementOptions {
    //the direction that we are trying to use to go around the obstacle; it's null if we re not trying to avoid an obstacle
    private static Direction bugDirection = null;
    private static final int ACCEPTABLE_LEVEL = 25; //robots only path with this much
    static void pathTowards(RobotController rc, MapLocation target) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        Direction d = currentLocation.directionTo(target);

        if(rc.canMove(d)){ // && !isObstacle(rc, d) -- this would be for pathfinding
            //easiest case! no obstacle
            rc.move(d);
        }else{
            if(rc.isMovementReady()){
                moveRandomly(rc);
            }
        }
    }
    /*/static void pathTowards(RobotController rc, MapLocation target) throws GameActionException{
        if (!rc.isMovementReady()){
            //if cooldown is too high, don't bother
            return;
        }

        MapLocation currentLocation = rc.getLocation();
        if(currentLocation.equals(target)){
            //were already at our goal!
            return;
        }


        Direction d = currentLocation.directionTo(target);
        if(rc.canMove(d) && !isObstacle(rc, d)){
            //easiest case! no obstacle
            rc.move(d);
            bugDirection = null;
        }else{
            //there IS an obstacle
            bugDirection = d; // make up this direction towards the goal


        }
        //now, try to avoid obstacle using bugDirection
        for(int i= 0; i < 8; i ++){ //repeat 8 times for all 8 possible directions
            if(rc.canMove(bugDirection) && !isObstacle(rc, bugDirection)){
                rc.move(bugDirection);
                bugDirection = bugDirection.rotateLeft();
                break;
            } else{
                bugDirection = bugDirection.rotateRight();
            }
        }



    }/*/
    //check to see if square is obstacle
    private static boolean isObstacle(RobotController rc, Direction d) throws GameActionException{
        MapLocation adjacentLocation = rc.getLocation().add(d);
        int rubbleOnLocation = rc.senseRubble(adjacentLocation);

        /*/if (rc.canSenseLocation(adjacentLocation.add(d))){
            RobotInfo possibleRobotAtLocation = rc.senseRobotAtLocation(adjacentLocation.add(d));
            if(possibleRobotAtLocation!= null){
                return true; //THIS IS TO AVOID THE ONE GLITCH WHERE HTINGS ARE BOUNCING BACK AND FORTH
            }
        }/*/

        return rubbleOnLocation > ACCEPTABLE_LEVEL && rubbleOnLocation < 90;
    }


    static void moveRandomly(RobotController rc) throws GameActionException{
        //move randomly
        boolean hasMoved = false;
        while(!hasMoved){
            Direction randoDirection = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
            if(rc.canMove(randoDirection)){
                rc.move(randoDirection);
                hasMoved = true;
            }
        }
    }

}
