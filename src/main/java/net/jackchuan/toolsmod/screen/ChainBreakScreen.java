package net.jackchuan.toolsmod.screen;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.jackchuan.toolsmod.event.ChainBreakEvent;
import net.jackchuan.toolsmod.packet.GeneratePacket;
import org.lwjgl.glfw.GLFW;

import static net.jackchuan.toolsmod.event.ChainBreakEvent.*;
import static net.jackchuan.toolsmod.event.ChainBreakEvent.generateShape;

public class ChainBreakScreen extends Screen {
    private final Screen parent;
    public ButtonWidget breakShape;
    public ButtonWidget restore;
    public ButtonWidget generateType;
    public ButtonWidget generate;
    public ButtonWidget directionConsidered;

    public ChainBreakScreen(Screen parent) {
        super(Text.literal("Chain Mining Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        breakShape = ButtonWidget.builder(Text.literal(ChainBreakEvent.breakShape), button -> {
                    switch (ChainBreakEvent.breakShape){
                        case "Default":
                            ChainBreakEvent.breakShape="Cube";
                            break;
                        case "Cube":
                            ChainBreakEvent.breakShape="Circle";
                            break;
                        case "Circle":
                            ChainBreakEvent.breakShape="Sphere";
                            break;
                        case "Sphere":
                            ChainBreakEvent.breakShape="Pyramid";
                            break;
                        case "Pyramid":
                            ChainBreakEvent.breakShape="Default";
                            break;
                    }
                    button.setMessage(Text.literal(ChainBreakEvent.breakShape));
                })
                .dimensions(width / 2 - 205, 20, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Select break shape")))
                .build();
        restore = ButtonWidget.builder(Text.literal("Restore"), button -> {
                    GeneratePacket.sendRestorePacket(ChainBreakEvent.targetBlock,ChainBreakEvent.brokenBlocks);
                })
                .dimensions(width / 2 + 5, 20, 200, 20)
                .tooltip(Tooltip.of(Text.literal("")))
                .build();


        int width = this.width / 2 - 100;
        int height = this.height / 4;

        // 添加滑动条来调整最大连锁数量
        this.addDrawableChild(new SliderWidget(width, height, 200, 20, Text.literal("Max Blocks: "+String.valueOf(ChainBreakEvent.maxCount)),ChainBreakEvent.maxCount/1024f) {

            @Override
            protected void updateMessage() {
                if(value==0)
                    this.setMessage(Text.literal("Max Blocks: " + (int)(this.value*1024+1)));
                else
                   this.setMessage(Text.literal("Max Blocks: " + (int)(this.value*1024)));
            }

            @Override
            protected void applyValue() {
                if(value==0)
                    ChainBreakEvent.maxCount = (int)( this.value*1024+1);
                else
                    ChainBreakEvent.maxCount = (int)( this.value*1024);
            }
        });

        height += 25;
        // 添加滑动条来调整优先半径
        this.addDrawableChild(new SliderWidget(width, height, 200, 20, Text.literal("Prefer Radius: "+((ChainBreakEvent.preferRadius==0)?"Auto":String.valueOf(ChainBreakEvent.preferRadius))),ChainBreakEvent.preferRadius/32f) {

            @Override
            protected void updateMessage() {
                if(value==0)
                    this.setMessage(Text.literal("Prefer Radius: Auto"));
                else
                    this.setMessage(Text.literal("Prefer Radius: " + (int)(this.value*32)));
            }

            @Override
            protected void applyValue() {
                if(value==0)
                    ChainBreakEvent.preferRadius = 0;
                else
                    ChainBreakEvent.preferRadius = (int)( this.value*32);
            }
        });
        height += 25;

        generateType = ButtonWidget.builder(Text.literal(generateShape), button -> {
                    switch (ChainBreakEvent.generateShape){
                        case "Cube":
                            ChainBreakEvent.generateShape="Circle";
                            break;
                        case "Circle":
                            ChainBreakEvent.generateShape="Sphere";
                            break;
                        case "Sphere":
                            ChainBreakEvent.generateShape="Pyramid";
                            break;
                        case "Pyramid":
                            ChainBreakEvent.generateShape="Cube";
                            break;
                    }
                    button.setMessage(Text.literal(ChainBreakEvent.generateShape));
                })
                .dimensions(this.width / 2 - 205, height, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Generate Shape")))
                .build();


        generate = ButtonWidget.builder(Text.literal("Generate"), button -> {
                    Vec3d lookDirection = client.player.getRotationVec(1.0F);
                    Direction direction = Direction.getFacing(lookDirection.x, lookDirection.y, lookDirection.z);
                    Iterable<ItemStack> stackList= client.player.getHandItems();
                    Block block=null;
                    for(ItemStack stack:stackList){
                       if(stack.getItem() instanceof BlockItem) {
                           block = ((BlockItem) stack.getItem()).getBlock();
                           break;
                       }
                    }
                    client.player.getServer();
                    if(block!=null) {
                        GeneratePacket.sendGeneratePacket(client.player.getPos(), direction, ChainBreakEvent.generateShape,block,ChainBreakEvent.radius);
                    }
                    else
                        client.player.sendMessage(Text.literal("there is no block in your hand!"));
                })
                .dimensions(this.width / 2 + 5, height, 200, 20)
                .tooltip(Tooltip.of(Text.literal("")))
                .build();

        height+=25;

        directionConsidered=ButtonWidget.builder(Text.literal("Direction Consideration"+ChainBreakEvent.directionConsidered), button -> {
                    if(ChainBreakEvent.directionConsidered){
                        ChainBreakEvent.directionConsidered = false;
                        button.setMessage(Text.literal("Direction Consideration : false"));
                    }else {
                        ChainBreakEvent.directionConsidered = true;
                        button.setMessage(Text.literal("Direction Consideration : true"));
                    }
                })
                .dimensions(this.width / 2 -200, height, 200, 20)
                .tooltip(Tooltip.of(Text.literal("")))
                .build();

        height += 25;
        // 添加滑动条来调整优先半径
        this.addDrawableChild(new SliderWidget(width, height, 200, 20, Text.literal("Radius: "+ChainBreakEvent.radius),ChainBreakEvent.radius/63f) {

            @Override
            protected void updateMessage() {
                    this.setMessage(Text.literal("Prefer Radius: " + (int)(this.value*63+1)));
            }

            @Override
            protected void applyValue() {
                ChainBreakEvent.radius = (int)( this.value*63+1);
            }
        });
        height += 25;

        // 添加按钮选择破坏形状
        addDrawableChild(breakShape);
        addDrawableChild(restore);
        addDrawableChild(generateType);
        addDrawableChild(generate);
        addDrawableChild(directionConsidered);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_N) { // 检测到N键被按下
            this.client.setScreen(this.parent); // 关闭当前界面，返回父界面
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers); // 继续处理其他键盘事件
    }

//    @Override
//    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//        super.render(context, mouseX, mouseY, delta);
//        this.renderBackground(context,mouseX,mouseY,delta);
////        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);
//        renderBackgroundTexture(context);
//    }
}


