package com.TBK.beyond_the_end.client.model;// Made with Blockbench 4.12.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.animacion.LightballAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class LightballModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(BeyondTheEnd.MODID, "lightball"), "main");
	private final ModelPart main;
	private final ModelPart lightning;
	private final ModelPart elecball;
	private final AnimationState loop=new AnimationState();

	public LightballModel(ModelPart root) {
		this.main = root.getChild("main");
		this.lightning = this.main.getChild("lightning");
		this.elecball = this.main.getChild("elecball");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));

		PartDefinition lightning = main.addOrReplaceChild("lightning", CubeListBuilder.create().texOffs(2, 0).addBox(-15.0F, -20.0F, 0.0F, 30.0F, 30.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition cube_r1 = lightning.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(2, 0).addBox(-15.0F, -15.0F, 0.0F, 30.0F, 30.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition elecball = main.addOrReplaceChild("elecball", CubeListBuilder.create().texOffs(12, 30).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(20, 52).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(1.15F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.loop.startIfStopped(entity.tickCount);
		this.animate(this.loop, LightballAnimation.IDLE,ageInTicks);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return this.main;
	}
}