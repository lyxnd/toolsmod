package net.jackchuan.toolsmod.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.jackchuan.toolsmod.event.ChainBreakEvent;

import java.util.HashSet;
import java.util.Set;

public class GeneratePacket {
    public static final Identifier ID = Identifier.of("dragonpro", "generate_structure");
    public static final Identifier ID1 = Identifier.of("dragonpro", "restore_structure");
    public static Block targetBlock;
    public static int radius=1;
    public static String generateShape = "Cube";
    public static boolean directionConsideration;


    public GeneratePacket(Vec3d pos, Direction direction, String generateShape) {
        this.generateShape = generateShape;
    }

    public static void sendGeneratePacket(Vec3d pos, Direction direction, String generateShape, Block block, int radius) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeEnumConstant(direction);
        buf.writeString(generateShape);
        buf.writeIdentifier(Registries.BLOCK.getId(block)); // 发送方块的Identifier
        buf.writeInt(radius);
        buf.writeBoolean(ChainBreakEvent.directionConsidered);



    }

    public static void sendRestorePacket(Block block,Set<BlockPos> brokenBlocks) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(brokenBlocks.size());
        for(BlockPos pos:brokenBlocks){
            buf.writeBlockPos(pos);
        }
        buf.writeIdentifier(Registries.BLOCK.getId(block)); // 发送方块的Identifier
//        ClientPlayNetworking.send(ID1, buf);
    }

    public static void receiveGeneratePacket() {
//        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
//            Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
//            Direction direction = buf.readEnumConstant(Direction.class);
//            generateShape = buf.readString(32767);
//            Identifier blockId = buf.readIdentifier();
//            targetBlock = Registries.BLOCK.get(blockId); // 从Identifier获取Block
//            radius = buf.readInt();
//            directionConsideration=buf.readBoolean();
//
//            server.execute(() -> {
//                World world = player.getWorld();
//                // Call your structure generation method here
//                generateStruct(world, pos, direction, targetBlock,directionConsideration);
//            });
//        });
    }

    public static void receiveRestorePacket() {
//        ServerPlayNetworking.registerGlobalReceiver(ID1, (server, player, handler, buf, responseSender) -> {
//            int n=buf.readInt();
//            Set<BlockPos> blocks=new HashSet<>();
//            for(int i=0;i<n;i++){
//                blocks.add(buf.readBlockPos());
//            }
//            System.out.println(blocks.size());
//            Identifier blockId = buf.readIdentifier();
//            targetBlock = Registries.BLOCK.get(blockId); // 从Identifier获取Block
//            server.execute(() -> {
//                World world = player.player().getWorld();
//                restoreBrokenBlock(world,targetBlock,blocks);
//            });
//        });
    }

    public static void restoreBrokenBlock(World world, Block block,Set<BlockPos> brokenBlocks){
        if(!world.isClient()){
            for (BlockPos pos : brokenBlocks) {
                world.setBlockState(pos, block.getDefaultState());
            }
        }
    }


    public static void registerReceiver() {
        receiveGeneratePacket();
        receiveRestorePacket();
    }

    public static void generateStruct(World world, Vec3d pos, Direction direction, Block block,boolean directionConsidered) {
        if(!world.isClient()){
            if (generateShape.equals("Cube")) {
                for (BlockPos pos1 : generateCube(pos, direction,directionConsidered)) {
                    world.setBlockState(pos1, block.getDefaultState());
                }
            } else if (generateShape.equals("Sphere")) {
                for (BlockPos pos1 : generateSphere(pos, direction,directionConsidered)) {
                    world.setBlockState(pos1, block.getDefaultState());
                }
            } else if (generateShape.equals("Circle")) {
                for(BlockPos pos1:generateCircle(pos, direction,directionConsidered)){
                    world.setBlockState(pos1, block.getDefaultState());
                }
            } else if (generateShape.equals("Pyramid")) {
                for (BlockPos pos1 : generatePyramid(pos, direction,directionConsidered)) {
                    world.setBlockState(pos1, block.getDefaultState());
                }
            }
        }
    }

    private static Set<BlockPos> generateCircle(Vec3d pos, Direction direction,boolean dir) {
        Set<BlockPos> blocks = new HashSet<>();
        int x= (int) pos.getX();
        int y= (int) pos.getY();
        int z= (int) pos.getZ();
        if(!dir)
            direction=Direction.UP;
        switch (direction){
            case UP:
                y+=3;
                for(int i=-radius;i<=radius;i++){
                    for(int j=-radius;j<=radius;j++){
                        if(i*i+j*j<radius*radius){
                            blocks.add(new BlockPos(x+i,y,z+j));
                        }
                    }
                }break;
            case DOWN:
                y-=3;
                for(int i=-radius;i<=radius;i++){
                    for(int j=-radius;j<=radius;j++){
                        if(i*i+j*j<radius*radius){
                            blocks.add(new BlockPos(x+i,y,z+j));
                        }
                    }
                }break;
            case NORTH:
                z-=3;
                for(int i=-radius;i<=radius;i++){
                    for(int j=-radius;j<=radius;j++){
                        if(i*i+j*j<=radius*radius){
                            blocks.add(new BlockPos(x+i,y+j,z));
                        }
                    }
                }break;
            case SOUTH:
                z+=3;
                for(int i=-radius;i<=radius;i++){
                    for(int j=-radius;j<=radius;j++){
                        if(i*i+j*j<=radius*radius){
                            blocks.add(new BlockPos(x+i,y+j,z));
                        }
                    }
                }break;
            case EAST:
                x+=3;
                for(int i=-radius;i<=radius;i++){
                    for(int j=-radius;j<=radius;j++){
                        if(i*i+j*j<=radius*radius){
                            blocks.add(new BlockPos(x,y+i,z+j));
                        }
                    }
                }break;
            case WEST:
                x-=3;
                for(int i=-radius;i<=radius;i++){
                    for(int j=-radius;j<=radius;j++){
                        if((i*i+j*j)<=radius*radius){
                            blocks.add(new BlockPos(x,y+i,z+j));
                        }
                    }
                }break;
        }
        return blocks;
    }

    private static Set<BlockPos> generateSphere(Vec3d pos, Direction direction,boolean dir) {
        Set<BlockPos> blocks = new HashSet<>();
        int x= (int) pos.getX();
        int y= (int) pos.getY();
        int z= (int) pos.getZ();
        if(!dir)
            direction=Direction.UP;
        switch (direction){
            case UP:
                y=y+radius+3;
                break;
            case DOWN:
                y=y-radius-3;
                break;
            case NORTH:
                z=z-radius-3;
                break;
            case SOUTH:
                z=z+radius+3;
                break;
            case WEST:
                x=x-radius-3;
                break;
            case EAST:
                x=x+radius+3;
                break;
        }
        for(int i=-radius;i<=radius;i++){
            for(int j=-radius;j<=radius;j++){
                for (int k=-radius;k<=radius;k++){
                    if((i*i+j*j+k*k)<radius*radius){
                        blocks.add(new BlockPos(x+i,y+j,z+k));
                    }
                }
            }
        }
        return blocks;
    }

    private static Set<BlockPos> generatePyramid(Vec3d pos, Direction direction,boolean dir) {
        int x= (int) pos.getX();
        int y= (int) pos.getY();
        int z= (int) pos.getZ();
        int r=radius;
        Set<BlockPos> generateBlocks = new HashSet<>();
        if(radius%2==0) {
            radius++;
        }
        if(!dir)
            direction=Direction.UP;
        switch (direction){
            case UP:
                y=y+radius+3;
                break;
            case DOWN:
                y=y-radius-3;
                break;
            case NORTH:
                z=z-radius-3;
                break;
            case SOUTH://x,y
                z=z+radius+3;
                break;
            case EAST://z,y
                x=x+radius+3;
                break;
            case WEST://z,y
                x=x-radius-3;
                break;
        }
        for(int i=0;i<radius+1;i++) {
            for(int j=-r; j<=r; j++) {
                for(int k=-r; k<=r; k++) {
                    switch (direction){
                        case UP:
                            generateBlocks.add(new BlockPos(x+j, y+i, z+k));
                            break;
                        case DOWN:
                            generateBlocks.add(new BlockPos(x+j, y-i, z+k));
                            break;
                        case NORTH:
                            generateBlocks.add(new BlockPos(x+j, y+k, z-i));
                            break;
                        case SOUTH://x,y
                            generateBlocks.add(new BlockPos(x+j, y+k, z+i));
                            break;
                        case EAST://z,y
                            generateBlocks.add(new BlockPos(x+i, y+j, z+k));
                            break;
                        case WEST://z,y
                            generateBlocks.add(new BlockPos(x-i, y+j, z+k));
                            break;
                    }
                }
            }
            r--;
        }
        return generateBlocks;
    }

    private static Set<BlockPos> generateCube(Vec3d pos, Direction direction,boolean dir) {
        int x= (int) pos.getX();
        int y= (int) pos.getY();
        int z= (int) pos.getZ();
        int l,r;
        Set<BlockPos> generateBlocks = new HashSet<>();
        if(radius%2==0) {
            l =  - radius / 2;
            r = radius / 2 - 1;
        }
        else {
            l = -(radius - 1) / 2;
            r = -l;
        }
        if(dir){
            switch (direction) {
                case UP:
                    y = y + radius + 3;
                    break;
                case DOWN:
                    y = y - radius - 3;
                    break;
                case NORTH://x,y
                    z = z - radius - 3;
                    break;
                case SOUTH://x,y
                    z = z + radius + 3;
                    break;
                case EAST://z,
                    x = x + radius + 3;
                    break;
                case WEST://z,y
                    x = x - radius - 3;
                    break;
            }
        }else
            y=y+radius+3;
        for(int i=l; i<=r; i++) {
            for(int j=l; j<=r; j++) {
                for(int k=l; k<=r; k++) {
                    generateBlocks.add(new BlockPos(x+i, y+j, z+k));
                }
            }
        }
        return generateBlocks;
    }
}
