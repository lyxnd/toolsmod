package net.jackchuan.toolsmod.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ChainBreakEvent {
    public static int maxCount =36;
    public static String breakShape = "Default";
    public static String generateShape = "Cube";
    private static int tCount;
    public static Block targetBlock;
    public static Set<BlockPos> brokenBlocks;
    public static int preferRadius=0;
    public static int radius=1;
    public static boolean directionConsidered=true;

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(ChainBreakEvent::onBlockBreak);
    }

    // 当玩家破坏方块时触发
    private static boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!world.isClient()) {
            // 检查玩家是否按住了Shift键
            if (player.isSneaking()) {
                targetBlock = state.getBlock();
                Vec3d lookDirection = player.getRotationVec(1.0F);
                Direction direction = Direction.getFacing(lookDirection.x, lookDirection.y, lookDirection.z);
                if (breakShape.equals("Default")) {
                    chainBreak(world, pos, targetBlock);
                } else if (breakShape.equals("Cube")) {
                    chainBreakCube(world, pos, targetBlock,player,direction);
                } else if (breakShape.equals("Sphere")) {
                    chainBreakSphere(world, pos, targetBlock,direction);
                } else if (breakShape.equals("Pyramid")) {
                    chainBreakPyramid(world, pos, targetBlock,direction);
                }else if (breakShape.equals("Circle")) {
                    chainBreakCircle(world, pos, targetBlock,direction);
                }
            }
        }
        return true; // 继续正常的破坏操作
    }

    private static void chainBreakPyramid(World world, BlockPos startPos, Block targetBlock,Direction direction) {
        int y=0;
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> posSet = new HashSet<>();
        Queue<BlockPos> toBreak = new LinkedList<>();
        int x = startPos.getX();
        int z=startPos.getZ();
        int l= (int) (Math.pow(3*maxCount,1/3f)+1);
        l=l/2;
        int t=l*(4*l*l-1)/3;
        while(t<maxCount){
            l++;
            t=l*(4*l*l-1)/3;
        }
        l-=2;//形状最优 unsolved
        if(preferRadius!=0) {
            l = preferRadius;
        }
        toBreak.add(startPos);
        while (!toBreak.isEmpty() && visited.size() < maxCount) {
            BlockPos currentPos = toBreak.poll();

            if (!visited.contains(currentPos) && world.getBlockState(currentPos).isOf(targetBlock)) {
                visited.add(currentPos);
                world.breakBlock(currentPos, true);
                if(toBreak.size()!=0)
                    continue;
                else {
                    if(y!=0)
                    {
                        switch(direction) {
                            case UP:
                                startPos = startPos.up();
                                break;
                            case DOWN:
                                startPos = startPos.down();
                                break;
                            case NORTH:
                                startPos = startPos.north();
                                break;
                            case SOUTH:
                                startPos = startPos.south();
                                break;
                            case EAST:
                                startPos = startPos.east();
                                break;
                            case WEST:
                                startPos = startPos.west();
                                break;
                        }
                        l--;
                    }
                    y++;

                }
                switch(direction) {
                    case UP,DOWN:
                        for(int i=-l;i<=l;i++){
                            for(int j=-l;j<=l;j++){
                                posSet.add(new BlockPos(x+i,startPos.getY(),z+j));
                            }
                        }
                        break;
                    case NORTH,SOUTH:
                        for(int i=-l;i<=l;i++){
                            for(int j=-l;j<=l;j++){
                                posSet.add(new BlockPos(x+i,startPos.getY()+j,z));
                            }
                        }
                        break;
                    case EAST,WEST:
                        for(int i=-l;i<=l;i++){
                            for(int j=-l;j<=l;j++){
                                posSet.add(new BlockPos(x,startPos.getY()+i,z+j));
                            }
                        }
                        break;
                }

                for (BlockPos newPos : posSet) {
                    if (!visited.contains(newPos) && world.getBlockState(newPos).isOf(targetBlock)) {
                        toBreak.add(newPos);
                    }
                }
            }
        }
        brokenBlocks=visited;
    }

    private static void chainBreak(World world, BlockPos startPos, Block targetBlock) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toBreak = new LinkedList<>();

        // 添加初始方块到待破坏队列
        toBreak.add(startPos);

        // 最大连锁破坏方块数，防止过多方块导致卡顿

        while (!toBreak.isEmpty() && visited.size() < maxCount) {
            BlockPos currentPos = toBreak.poll();

            if (!visited.contains(currentPos) && world.getBlockState(currentPos).isOf(targetBlock)) {
                visited.add(currentPos);
                world.breakBlock(currentPos, true);

                // 检查3x3x3范围内的方块
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos newPos = currentPos.add(x, y, z);
                            if (!visited.contains(newPos) && world.getBlockState(newPos).isOf(targetBlock)) {
                                toBreak.add(newPos);
                            }
                        }
                    }
                }
            }
        }
        brokenBlocks=visited;
    }

    // 使用队列递归破坏相连的同种方块（立方体形状）
    private static void chainBreakCube(World world, BlockPos startPos, Block targetBlock, PlayerEntity player, Direction direction) {
        int y=0;
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toBreak = new LinkedList<>();
        toBreak.add(startPos);
        while (!toBreak.isEmpty() && visited.size() < maxCount) {
            BlockPos currentPos = toBreak.poll();

            if (!visited.contains(currentPos) && world.getBlockState(currentPos).getBlock() == targetBlock) {
                visited.add(currentPos);
                world.breakBlock(currentPos, true);
                if(toBreak.size()!=0)
                    continue;
                else {
                    if(y!=0)
                    {
                        switch(direction) {
                            case UP:
                                startPos = startPos.up();
                                break;
                            case DOWN:
                                startPos=startPos.down();
                                break;
                            case NORTH:
                                startPos = startPos.north();
                                break;
                            case SOUTH:
                                startPos = startPos.south();
                                break;
                            case EAST:
                                startPos = startPos.east();
                                break;
                            case WEST:
                                startPos = startPos.west();
                                break;
                        }
                    }
                    y++;
                }
                for (BlockPos newPos : getNeighborsInDirection(startPos, direction,visited)) {
                    if (!visited.contains(newPos) && world.getBlockState(newPos).getBlock() == targetBlock) {
                        toBreak.add(newPos);
                    }
                }
            }
        }
        brokenBlocks=visited;
    }

    // 使用队列递归破坏相连的同种方块（球体形状）
    private static void chainBreakSphere(World world, BlockPos startPos, Block targetBlock, Direction direction) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toBreak = new LinkedList<>();
        int y=0;
        toBreak.add(startPos);

        while (!toBreak.isEmpty() && visited.size() < maxCount) {
            BlockPos currentPos = toBreak.poll();

            if (!visited.contains(currentPos) && world.getBlockState(currentPos).getBlock() == targetBlock) {
                visited.add(currentPos);
                world.breakBlock(currentPos, true);
                if(toBreak.size()!=0)
                    continue;
                else {
                    if(y!=0)
                    {
                        switch(direction) {
                            case UP:
                                startPos = startPos.up();
                                break;
                            case DOWN:
                                startPos=startPos.down();
                                break;
                            case NORTH:
                                startPos = startPos.north();
                                break;
                            case SOUTH:
                                startPos = startPos.south();
                                break;
                            case EAST:
                                startPos = startPos.east();
                                break;
                            case WEST:
                                startPos = startPos.west();
                                break;
                        }
                    }
                    y++;
                }

                for (BlockPos newPos : getNeighborsSphere(startPos,direction,visited)) {
                    if (!visited.contains(newPos) && world.getBlockState(newPos).getBlock() == targetBlock) {
                        toBreak.add(newPos);
                    }
                }
            }
        }
        brokenBlocks=visited;
    }

    // 获取立方体邻居方块
    private static Iterable<BlockPos> getNeighborsInDirection(BlockPos pos, Direction direction, Set<BlockPos> visited) {
        int x,y,z,r;
        x=pos.getX();
        y=pos.getY();
        z=pos.getZ();
        tCount= (int) Math.pow(maxCount,1/3f);
        if(preferRadius!=0)
            tCount=preferRadius;
        tCount= tCount%2==0?tCount/2:(tCount-1)/2;
        if(tCount%2==0)
            r=tCount-1;
        else
            r=tCount;
        Set<BlockPos> neighbors = new HashSet<>();
        switch (direction) {
            case UP, DOWN:
                for(int i=-tCount; i<=r; i++) {
                    for(int j=-tCount; j<=r; j++) {
                        pos=new BlockPos(x+i,y,z+j);
                        neighbors.add(pos);
                    }
                }
                break;
            case EAST, WEST:
                for(int i=-tCount; i<=r; i++) {
                    for(int j=-tCount; j<=r; j++) {
                        pos=new BlockPos(x,y+i,z+j);
                        neighbors.add(pos);
                    }
                }
                break;
            case NORTH, SOUTH:
                for(int i=-tCount; i<=r; i++) {
                    for(int j=-tCount; j<=r; j++) {
                        pos=new BlockPos(x+i,y+j,z);
                        neighbors.add(pos);
                    }
                }
                break;
        }

        return neighbors;
    }

    // 获取球体邻居方块
    private static Iterable<BlockPos> getNeighborsSphere(BlockPos pos , Direction direction, Set<BlockPos> visited) {
        int x,y,z;
        x=pos.getX();
        y=pos.getY();
        z=pos.getZ();
//        tCount=maxCount-visited.size();
        tCount=3*maxCount/2;
        tCount= (int) Math.pow(tCount/Math.PI,1/3f);//半径
        System.out.println("Sphere r:"+tCount);
        if(preferRadius!=0)
            tCount=preferRadius;
        Set<BlockPos> neighbors = new HashSet<>();
        for(int i=-tCount; i<=tCount; i++) {
            for(int j=-tCount; j<=tCount; j++) {
                for(int l=-tCount;l<=tCount;l++){
                    float d=i*i+j*j+l*l;
                    if(d<=tCount*tCount) {
                        switch(direction) {
                            case UP:
                                if(j>=0){
                                    pos = new BlockPos(x + i, y + j, z + l);
                                    neighbors.add(pos);
                                }break;
                            case DOWN:
                                if(j<=0){
                                    pos = new BlockPos(x + i, y + j, z + l);
                                    neighbors.add(pos);
                                }break;
                            case NORTH:
                                if(l>=0){
                                    pos = new BlockPos(x + i, y + j, z + l);
                                    neighbors.add(pos);
                                }break;
                            case SOUTH:
                                if(l<=0){
                                    pos = new BlockPos(x + i, y + j, z + l);
                                    neighbors.add(pos);
                                }break;
                            case EAST:
                                if(i>=0){
                                    pos = new BlockPos(x + i, y + j, z + l);
                                    neighbors.add(pos);
                                }break;
                            case WEST:
                                if(i<=0){
                                    pos = new BlockPos(x + i, y + j, z + l);
                                    neighbors.add(pos);
                                }break;
                        }
                    }
                }

            }
        }
        return neighbors;
    }

    private static void chainBreakCircle(World world, BlockPos startPos, Block targetBlock,Direction direction) {
        Set<BlockPos> positionsToBreak = new HashSet<>();
        BlockState startBlockState = world.getBlockState(startPos);

        int radius = (int) Math.round(Math.sqrt(maxCount / Math.PI));
        if(preferRadius!=0)
            tCount=preferRadius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos newPos = startPos.add(x, 0, z);
                if (positionsToBreak.size() >= maxCount) {
                    break;
                }
                if (isWithinCircle(newPos, startPos, radius) && world.getBlockState(newPos).isOf(targetBlock)) {
                    positionsToBreak.add(newPos);
                }
            }
        }

        for (BlockPos pos : positionsToBreak) {
            world.breakBlock(pos, true);
        }
        brokenBlocks=positionsToBreak;
    }

    // 判断方块是否在圆形范围内
    private static boolean isWithinCircle(BlockPos pos, BlockPos center, int radius) {
        double dx = pos.getX() - center.getX();
        double dz = pos.getZ() - center.getZ();
        return (dx * dx + dz * dz) <= radius * radius;
    }
}
