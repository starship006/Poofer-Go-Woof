package PooferTwo;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;


public class MinerStrategy {
    static Direction exploreDir = null; //

    //called for every Miner every turn
    static void runMiner(RobotController rc) throws GameActionException {
        //Miner code
        //establish random direction for exploration
        if(exploreDir==null){
            RobotPlayer.rng.setSeed(rc.getID());
            exploreDir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)]; //set a random direction for the miner to explore
        }

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

        // Try to move strategically.
        int visionRadius = rc.getType().visionRadiusSquared;
        MapLocation[] nearbyLocations = rc.getAllLocationsWithinRadiusSquared(me,visionRadius);
        MapLocation targetLocation = null;
        int distanceToTarget = 9000;
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
        if (targetLocation!=null){
            MovementOptions.pathTowards(rc, targetLocation);
        }

        //go in exploration direction
        if(targetLocation==null && rc.isMovementReady()){
            if(rc.canMove(exploreDir)){
                rc.move(exploreDir);
            }else if(!rc.onTheMap(rc.getLocation().add(exploreDir))){
                exploreDir = exploreDir.opposite();
                rc.setIndicatorString(exploreDir.toString());
            }else{
                MovementOptions.moveRandomly(rc);
            }
        }
    }
}
