package codyfirstrobo;

import battlecode.common.*;
import java.util.Random;


strictfp class MinerStrategy {
    static Direction exploreDir = null;

    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runMiner(RobotController rc) throws GameActionException {
        if(exploreDir == null){
            RobotPlayer.rng.setSeed(rc.getID());
            exploreDir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)]; //set a random direction for the miner to explore
        }
        rc.setIndicatorString(exploreDir.toString());

        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation)>1) {
                    rc.mineLead(mineLocation);
                }
            }
        }

        int visionRadius = rc.getType().visionRadiusSquared;
        MapLocation[] nearbyLocations = rc.getAllLocationsWithinRadiusSquared(me,visionRadius);

        MapLocation targetLocation = null;
        int distanceToTarget = 999;

        for (MapLocation tryLocation:nearbyLocations){
            if (rc.senseLead(tryLocation) > 1  || rc.senseGold(tryLocation) > 0) {
                int distanceTo = me.distanceSquaredTo(tryLocation);
                if (distanceTo<distanceToTarget){
                    targetLocation = tryLocation;
                    distanceToTarget = distanceTo;
                    break;
                }

            }
        }


        if (targetLocation!= null) {
            Direction toMove = me.directionTo(targetLocation);
            if (rc.canMove(toMove)) {
                rc.move(toMove);
            }
        }else{
            //try to explore
            if(rc.canMove(exploreDir)){
                rc.move(exploreDir);
            }else if(!rc.onTheMap(rc.getLocation().add(exploreDir))){
                exploreDir = exploreDir.opposite();
            }

            // try to move randomly.
            int directionIndex = RobotPlayer.rng.nextInt(RobotPlayer.directions.length);
            Direction dir = RobotPlayer.directions[directionIndex];
            if (rc.canMove(dir)) {
                rc.move(dir);
                System.out.println("I moved!");
            }
        }

    }
}
