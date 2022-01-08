package rishifirstrobo;

import battlecode.common.*;
import java.util.Random;

strictfp class MinerStrategy {
    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runMiner(RobotController rc) throws GameActionException {
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
                while (rc.canMineLead(mineLocation) || rc.senseLead(mineLocation) > 1) {
                    rc.mineLead(mineLocation);
                }
            }
        }

        int visionRadius = rc.getType().visionRadiusSquared;
        MapLocation[] nearbyLocations = rc.getAllLocationsWithinRadiusSquared(me, visionRadius);
        int distanceToTarget = Integer.MAX_VALUE;

        MapLocation targetLocation = null;
        // For each nearby location
        for (MapLocation tryLocation : nearbyLocations) {
            // resource there??
            if (rc.senseLead(tryLocation) > 1 || rc.senseGold(tryLocation) > 0) {
                // Yes? Consider going there
                int distanceTo = me.distanceSquaredTo(tryLocation);
                if (distanceTo < distanceToTarget) {
                    targetLocation = tryLocation;
                    distanceToTarget = distanceTo;
                }
                break;
            }
        }
        // We have a target location! Let's move  towards it
        if (targetLocation != null) {
            // Don't use MapLocation.directionTo(targetLocation) !
            Direction toMove = me.directionTo(targetLocation);
            if (rc.canMove(toMove)) {
                rc.move(toMove);
            }
        }


        // Also try to move randomly.
        int directionIndex = RobotPlayer.rng.nextInt(RobotPlayer.directions.length);
        Direction dir = RobotPlayer.directions[directionIndex];
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
        }
    }

}
