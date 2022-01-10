package PooferOne;

import battlecode.common.*;


public class SoldierStrategy {
    //called for every Soldier every turn

    static final int AMOUNT_OF_ENEMIES_TO_BE_A_TARGET = 3;
    static final int AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET = 200;
    static Direction exploreDir = null; //
    static MapLocation targetLocation = null;


    static void runSoldier(RobotController rc) throws GameActionException {
        //TODO make soldiers charge right towards enemy after first sighting?
        //Soldier Find Danger Mode
        //init code
        if(exploreDir == null){
            RobotPlayer.rng.setSeed(rc.getID());
            exploreDir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
            rc.setIndicatorString(exploreDir.toString());
        } else if (rc.getRoundNum() % AMOUNT_OF_CHECK_ROUNDS_FOR_TARGET == 0){
            int position = rc.readSharedArray(3); // if there was enemies detected
            if (position > 0){
                targetLocation = new MapLocation(position % 100, position/100);
            }
            position = rc.readSharedArray(4); //if there was an archon detected
            if (position > 0){
                targetLocation = new MapLocation(position % 100, position/100);
            }
        }

        if (targetLocation != null){
            //pathfind
            MovementOptions.pathTowards(rc, targetLocation);
        }
        if(rc.isMovementReady()){  //this is if the robot is already close enough. now we just explore
            //explore in a direction
            if(rc.canMove(exploreDir)){
                rc.move(exploreDir);
            }else if(!rc.onTheMap(rc.getLocation().add(exploreDir))){
                exploreDir = exploreDir.opposite();
            }else if(rc.isMovementReady()){
                MovementOptions.moveRandomly(rc);
            }
        }



        //try to attack enemy + signal enemy spotted
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0 ){
            MapLocation toAttack = enemies[0].location;

            if(enemies[0].getType() == RobotType.ARCHON){
                rc.writeSharedArray(4, enemies[0].location.y * 100 + enemies[0].location.x);
            }

            int PositionNumber = toAttack.y * 100 + toAttack.x;
            rc.writeSharedArray(2,PositionNumber);


            if(rc.canAttack(toAttack)){
                rc.attack(toAttack);
            }



            if (enemies.length > AMOUNT_OF_ENEMIES_TO_BE_A_TARGET - 1){
                rc.writeSharedArray(3,rc.getLocation().y * 100 + rc.getLocation().x);
            }
        }




    }
}
