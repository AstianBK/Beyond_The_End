package com.TBK.beyond_the_end.client.model;// Made with Blockbench 4.12.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.animacion.JellyfishAnim;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class JellyfishModel<T extends JellyfishEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "jellyfish"), "main");
	private final ModelPart truemain;
	private final ModelPart main;
	private final ModelPart jellyfish;
	private final ModelPart Head;
	private final ModelPart Eyes;
	private final ModelPart Teeth;
	private final ModelPart FrontTeeth;
	private final ModelPart Teeth1;
	private final ModelPart Teeth2;
	private final ModelPart Teeth3;
	private final ModelPart Teeth4;
	private final ModelPart Teeth7;
	private final ModelPart Teeth8;
	private final ModelPart Teeth5;
	private final ModelPart Teeth6;
	private final ModelPart BackTeeth;
	private final ModelPart Teeth9;
	private final ModelPart Teeth10;
	private final ModelPart Teeth11;
	private final ModelPart Teeth12;
	private final ModelPart Teeth13;
	private final ModelPart Teeth14;
	private final ModelPart RightTeeth;
	private final ModelPart Teeth19;
	private final ModelPart Teeth17;
	private final ModelPart Teeth18;
	private final ModelPart Teeth20;
	private final ModelPart Teeth16;
	private final ModelPart Teeth15;
	private final ModelPart LeftTeeth;
	private final ModelPart Teeth21;
	private final ModelPart Teeth22;
	private final ModelPart Teeth23;
	private final ModelPart Teeth24;
	private final ModelPart Teeth25;
	private final ModelPart Teeth26;
	private final ModelPart Teeth27;
	private final ModelPart Teeth31;
	private final ModelPart Teeth32;
	private final ModelPart Teeth33;
	private final ModelPart Teeth34;
	private final ModelPart Teeth35;
	private final ModelPart Teeth28;
	private final ModelPart Teeth29;
	private final ModelPart Teeth30;
	private final ModelPart Tendrils3;
	private final ModelPart LowerTendril3;
	private final ModelPart LowerTendril1Section7;
	private final ModelPart LowerTendril1Section8;
	private final ModelPart LowerMiddleTendril;
	private final ModelPart LowerTendril1Section9;
	private final ModelPart Tendrils4;
	private final ModelPart LowerTendril4;
	private final ModelPart LowerTendril1Section5;
	private final ModelPart LowerTendril1Section6;
	private final ModelPart MiddleTentacle;
	private final ModelPart LowerTendril1Section10;
	private final ModelPart Tendrils2;
	private final ModelPart LowerTendril2;
	private final ModelPart LowerTendril1Section2;
	private final ModelPart LowerTendril1Section3;
	private final ModelPart LowerTendrilMiddle;
	private final ModelPart LowerTendril1Section4;
	private final ModelPart Tendrils5;
	private final ModelPart LowerTendril5;
	private final ModelPart LowerTendril1Section11;
	private final ModelPart LowerTendril1Section12;
	private final ModelPart LowerTendrilSectionMiddle;
	private final ModelPart LowerTendril1Section13;
	private final List<ModelPart> glowingPart;


	public JellyfishModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.truemain = root.getChild("truemain");
		this.main = this.truemain.getChild("main");
		this.jellyfish = this.main.getChild("jellyfish");
		this.Head = this.jellyfish.getChild("Head");
		this.Eyes = this.Head.getChild("Eyes");
		this.Teeth = this.Head.getChild("Teeth");
		this.FrontTeeth = this.Teeth.getChild("FrontTeeth");
		this.Teeth1 = this.FrontTeeth.getChild("Teeth1");
		this.Teeth2 = this.FrontTeeth.getChild("Teeth2");
		this.Teeth3 = this.FrontTeeth.getChild("Teeth3");
		this.Teeth4 = this.FrontTeeth.getChild("Teeth4");
		this.Teeth7 = this.FrontTeeth.getChild("Teeth7");
		this.Teeth8 = this.FrontTeeth.getChild("Teeth8");
		this.Teeth5 = this.FrontTeeth.getChild("Teeth5");
		this.Teeth6 = this.FrontTeeth.getChild("Teeth6");
		this.BackTeeth = this.Teeth.getChild("BackTeeth");
		this.Teeth9 = this.BackTeeth.getChild("Teeth9");
		this.Teeth10 = this.BackTeeth.getChild("Teeth10");
		this.Teeth11 = this.BackTeeth.getChild("Teeth11");
		this.Teeth12 = this.BackTeeth.getChild("Teeth12");
		this.Teeth13 = this.BackTeeth.getChild("Teeth13");
		this.Teeth14 = this.BackTeeth.getChild("Teeth14");
		this.RightTeeth = this.Teeth.getChild("RightTeeth");
		this.Teeth19 = this.RightTeeth.getChild("Teeth19");
		this.Teeth17 = this.RightTeeth.getChild("Teeth17");
		this.Teeth18 = this.RightTeeth.getChild("Teeth18");
		this.Teeth20 = this.RightTeeth.getChild("Teeth20");
		this.Teeth16 = this.RightTeeth.getChild("Teeth16");
		this.Teeth15 = this.RightTeeth.getChild("Teeth15");
		this.LeftTeeth = this.Teeth.getChild("LeftTeeth");
		this.Teeth21 = this.LeftTeeth.getChild("Teeth21");
		this.Teeth22 = this.LeftTeeth.getChild("Teeth22");
		this.Teeth23 = this.LeftTeeth.getChild("Teeth23");
		this.Teeth24 = this.LeftTeeth.getChild("Teeth24");
		this.Teeth25 = this.LeftTeeth.getChild("Teeth25");
		this.Teeth26 = this.LeftTeeth.getChild("Teeth26");
		this.Teeth27 = this.Teeth.getChild("Teeth27");
		this.Teeth31 = this.Teeth.getChild("Teeth31");
		this.Teeth32 = this.Teeth.getChild("Teeth32");
		this.Teeth33 = this.Teeth.getChild("Teeth33");
		this.Teeth34 = this.Teeth.getChild("Teeth34");
		this.Teeth35 = this.Teeth.getChild("Teeth35");
		this.Teeth28 = this.Teeth.getChild("Teeth28");
		this.Teeth29 = this.Teeth.getChild("Teeth29");
		this.Teeth30 = this.Teeth.getChild("Teeth30");
		this.Tendrils3 = this.jellyfish.getChild("Tendrils3");
		this.LowerTendril3 = this.Tendrils3.getChild("LowerTendril3");
		this.LowerTendril1Section7 = this.LowerTendril3.getChild("LowerTendril1Section7");
		this.LowerTendril1Section8 = this.LowerTendril1Section7.getChild("LowerTendril1Section8");
		this.LowerMiddleTendril = this.LowerTendril1Section8.getChild("LowerMiddleTendril");
		this.LowerTendril1Section9 = this.LowerMiddleTendril.getChild("LowerTendril1Section9");
		this.Tendrils4 = this.jellyfish.getChild("Tendrils4");
		this.LowerTendril4 = this.Tendrils4.getChild("LowerTendril4");
		this.LowerTendril1Section5 = this.LowerTendril4.getChild("LowerTendril1Section5");
		this.LowerTendril1Section6 = this.LowerTendril1Section5.getChild("LowerTendril1Section6");
		this.MiddleTentacle = this.LowerTendril1Section6.getChild("MiddleTentacle");
		this.LowerTendril1Section10 = this.MiddleTentacle.getChild("LowerTendril1Section10");
		this.Tendrils2 = this.jellyfish.getChild("Tendrils2");
		this.LowerTendril2 = this.Tendrils2.getChild("LowerTendril2");
		this.LowerTendril1Section2 = this.LowerTendril2.getChild("LowerTendril1Section2");
		this.LowerTendril1Section3 = this.LowerTendril1Section2.getChild("LowerTendril1Section3");
		this.LowerTendrilMiddle = this.LowerTendril1Section3.getChild("LowerTendrilMiddle");
		this.LowerTendril1Section4 = this.LowerTendrilMiddle.getChild("LowerTendril1Section4");
		this.Tendrils5 = this.jellyfish.getChild("Tendrils5");
		this.LowerTendril5 = this.Tendrils5.getChild("LowerTendril5");
		this.LowerTendril1Section11 = this.LowerTendril5.getChild("LowerTendril1Section11");
		this.LowerTendril1Section12 = this.LowerTendril1Section11.getChild("LowerTendril1Section12");
		this.LowerTendrilSectionMiddle = this.LowerTendril1Section12.getChild("LowerTendrilSectionMiddle");
		this.LowerTendril1Section13 = this.LowerTendrilSectionMiddle.getChild("LowerTendril1Section13");
		this.glowingPart = ImmutableList.of(
				this.main
		);
	}

	public List<ModelPart> getBody(){
		return this.glowingPart;
	}

	public List<ModelPart> getEye(){
		return List.of(this.Eyes);
	}
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition truemain = partdefinition.addOrReplaceChild("truemain", CubeListBuilder.create(), PartPose.offset(0.0F, -28.0F, 0.0F));

		PartDefinition main = truemain.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition jellyfish = main.addOrReplaceChild("jellyfish", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Head = jellyfish.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 52).addBox(-12.5F, 3.6667F, -12.5F, 25.0F, 4.0F, 25.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-11.0F, -2.3333F, -11.0F, 22.0F, 6.0F, 22.0F, new CubeDeformation(0.0F))
				.texOffs(0, 28).addBox(-7.5F, -10.3333F, -7.5F, 15.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.3333F, 0.0F));

		PartDefinition Eyes = Head.addOrReplaceChild("Eyes", CubeListBuilder.create(), PartPose.offset(-8.5F, 0.6667F, -11.4F));

		PartDefinition Eye3_r1 = Eyes.addOrReplaceChild("Eye3_r1", CubeListBuilder.create().texOffs(3, 2).mirror().addBox(-4.5F, -2.0F, 0.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(14.9117F, 4.3705F, -1.15F, 0.0F, 0.0F, -0.1745F));

		PartDefinition Eye2_r1 = Eyes.addOrReplaceChild("Eye2_r1", CubeListBuilder.create().texOffs(3, 2).mirror().addBox(-4.5F, -2.0F, 0.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(16.0F, 0.0F, 0.375F, 0.0F, 0.0F, -0.3054F));

		PartDefinition Pupil2_r1 = Eyes.addOrReplaceChild("Pupil2_r1", CubeListBuilder.create().texOffs(0, 5).mirror().addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.27F)).mirror(false), PartPose.offsetAndRotation(13.2773F, 1.4093F, 0.6F, 0.0F, 0.0F, -0.1745F));

		PartDefinition Pupil1_r1 = Eyes.addOrReplaceChild("Pupil1_r1", CubeListBuilder.create().texOffs(0, 5).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.27F)), PartPose.offsetAndRotation(3.7227F, 1.4093F, 0.6F, 0.0F, 0.0F, 0.1745F));

		PartDefinition Pupil3_r1 = Eyes.addOrReplaceChild("Pupil3_r1", CubeListBuilder.create().texOffs(0, 5).mirror().addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.27F)).mirror(false), PartPose.offsetAndRotation(12.0523F, 5.4093F, -0.925F, 0.0F, 0.0F, -0.1745F));

		PartDefinition Pupil2_r2 = Eyes.addOrReplaceChild("Pupil2_r2", CubeListBuilder.create().texOffs(0, 5).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.27F)), PartPose.offsetAndRotation(4.9477F, 5.4093F, -0.925F, 0.0F, 0.0F, 0.1745F));

		PartDefinition Eye2_r2 = Eyes.addOrReplaceChild("Eye2_r2", CubeListBuilder.create().texOffs(3, 2).addBox(-3.5F, -2.0F, 0.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0883F, 4.3705F, -1.15F, 0.0F, 0.0F, 0.1745F));

		PartDefinition Eye1_r1 = Eyes.addOrReplaceChild("Eye1_r1", CubeListBuilder.create().texOffs(3, 2).addBox(-3.5F, -2.0F, 0.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, 0.375F, 0.0F, 0.0F, 0.3054F));

		PartDefinition Teeth = Head.addOrReplaceChild("Teeth", CubeListBuilder.create(), PartPose.offset(-1.125F, -0.45F, 0.0F));

		PartDefinition FrontTeeth = Teeth.addOrReplaceChild("FrontTeeth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Teeth1 = FrontTeeth.addOrReplaceChild("Teeth1", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offset(-0.375F, 9.5196F, -10.1875F));

		PartDefinition cube_r1 = Teeth1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth2 = FrontTeeth.addOrReplaceChild("Teeth2", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -2.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offset(2.625F, 9.5196F, -10.1875F));

		PartDefinition cube_r2 = Teeth2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, -0.3875F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth3 = FrontTeeth.addOrReplaceChild("Teeth3", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(5.775F, 9.3696F, -10.1875F, 0.0F, 0.0F, -0.1309F));

		PartDefinition cube_r3 = Teeth3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth4 = FrontTeeth.addOrReplaceChild("Teeth4", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offset(8.625F, 9.5196F, -10.1875F));

		PartDefinition cube_r4 = Teeth4.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth7 = FrontTeeth.addOrReplaceChild("Teeth7", CubeListBuilder.create(), PartPose.offset(-6.375F, 9.5196F, -10.1875F));

		PartDefinition cube_r5 = Teeth7.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, -0.4125F, -0.4375F, 0.0F, 0.0F, -0.0873F));

		PartDefinition cube_r6 = Teeth7.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.05F, 0.3125F, -0.6375F, 0.0F, 0.0F, 0.6981F));

		PartDefinition Teeth8 = FrontTeeth.addOrReplaceChild("Teeth8", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offset(-3.375F, 8.5196F, -10.1875F));

		PartDefinition cube_r7 = Teeth8.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth5 = FrontTeeth.addOrReplaceChild("Teeth5", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(11.25F, 9.5196F, -10.1875F, 0.0F, 0.0F, 0.2618F));

		PartDefinition cube_r8 = Teeth5.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth6 = FrontTeeth.addOrReplaceChild("Teeth6", CubeListBuilder.create().texOffs(4, 10).mirror().addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(-9.0F, 9.5196F, -10.1875F, 0.0F, 0.0F, -0.2618F));

		PartDefinition cube_r9 = Teeth6.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(4, 14).mirror().addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, -0.7854F));

		PartDefinition BackTeeth = Teeth.addOrReplaceChild("BackTeeth", CubeListBuilder.create(), PartPose.offsetAndRotation(1.125F, 9.1167F, 11.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Teeth9 = BackTeeth.addOrReplaceChild("Teeth9", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.5F, 0.4029F, 0.7875F));

		PartDefinition cube_r10 = Teeth9.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth10 = BackTeeth.addOrReplaceChild("Teeth10", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -2.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offset(1.5F, 0.4029F, 0.7125F));

		PartDefinition cube_r11 = Teeth10.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, -0.3875F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth11 = BackTeeth.addOrReplaceChild("Teeth11", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(4.65F, 0.2529F, 0.8125F, 0.0F, 0.0F, -0.1309F));

		PartDefinition cube_r12 = Teeth11.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth12 = BackTeeth.addOrReplaceChild("Teeth12", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offset(7.5F, 0.4029F, 1.7125F));

		PartDefinition cube_r13 = Teeth12.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth13 = BackTeeth.addOrReplaceChild("Teeth13", CubeListBuilder.create(), PartPose.offset(-7.5F, 0.4029F, -0.1875F));

		PartDefinition cube_r14 = Teeth13.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, -0.4125F, 1.4125F, 0.0F, 0.0F, -0.0873F));

		PartDefinition cube_r15 = Teeth13.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.05F, 0.3125F, 1.2125F, 0.0F, 0.0F, 0.6981F));

		PartDefinition Teeth14 = BackTeeth.addOrReplaceChild("Teeth14", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, 0.5625F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offset(-4.5F, -0.5971F, -0.1875F));

		PartDefinition cube_r16 = Teeth14.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, 0.3625F, 0.0F, 0.0F, 0.7854F));

		PartDefinition RightTeeth = Teeth.addOrReplaceChild("RightTeeth", CubeListBuilder.create(), PartPose.offset(-9.875F, 9.1167F, 0.0F));

		PartDefinition Teeth19 = RightTeeth.addOrReplaceChild("Teeth19", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, 0.8279F, 7.5375F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r17 = Teeth19.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth17 = RightTeeth.addOrReplaceChild("Teeth17", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, 0.1779F, -1.4625F, 1.5708F, 1.4835F, 1.5708F));

		PartDefinition cube_r18 = Teeth17.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth18 = RightTeeth.addOrReplaceChild("Teeth18", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, -0.0221F, 4.5375F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r19 = Teeth18.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth20 = RightTeeth.addOrReplaceChild("Teeth20", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, 0.6779F, 1.5375F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r20 = Teeth20.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth16 = RightTeeth.addOrReplaceChild("Teeth16", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, -0.0221F, -4.4625F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r21 = Teeth16.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6125F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth15 = RightTeeth.addOrReplaceChild("Teeth15", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, 0.4029F, -7.4625F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r22 = Teeth15.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition LeftTeeth = Teeth.addOrReplaceChild("LeftTeeth", CubeListBuilder.create(), PartPose.offsetAndRotation(12.125F, 9.1167F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Teeth21 = LeftTeeth.addOrReplaceChild("Teeth21", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, 0.8279F, 7.5375F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r23 = Teeth21.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth22 = LeftTeeth.addOrReplaceChild("Teeth22", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, 0.1779F, -1.4625F, 1.5708F, 1.4835F, 1.5708F));

		PartDefinition cube_r24 = Teeth22.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth23 = LeftTeeth.addOrReplaceChild("Teeth23", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, -0.0221F, 4.5375F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r25 = Teeth23.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth24 = LeftTeeth.addOrReplaceChild("Teeth24", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, 0.6779F, 1.5375F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r26 = Teeth24.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth25 = LeftTeeth.addOrReplaceChild("Teeth25", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, -0.0221F, -4.4625F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r27 = Teeth25.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6125F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth26 = LeftTeeth.addOrReplaceChild("Teeth26", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.6125F, -0.4375F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.65F, 0.4029F, -7.4625F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r28 = Teeth26.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.6125F, -0.6375F, 0.0F, 0.0F, 0.7854F));

		PartDefinition Teeth27 = Teeth.addOrReplaceChild("Teeth27", CubeListBuilder.create(), PartPose.offset(-1.725F, 8.6446F, -8.1875F));

		PartDefinition cube_r29 = Teeth27.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.725F, 0.3625F, -0.4375F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r30 = Teeth27.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-0.725F, 1.0875F, -0.6375F, -0.1872F, -0.1841F, 0.8027F));

		PartDefinition Teeth31 = Teeth.addOrReplaceChild("Teeth31", CubeListBuilder.create(), PartPose.offset(-5.625F, 8.6446F, 3.8125F));

		PartDefinition cube_r31 = Teeth31.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.725F, 0.9375F, -0.7625F, 0.0F, 0.1309F, 0.0F));

		PartDefinition cube_r32 = Teeth31.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-0.725F, 1.6625F, -0.9625F, 0.0928F, 0.0924F, 0.7897F));

		PartDefinition Teeth32 = Teeth.addOrReplaceChild("Teeth32", CubeListBuilder.create(), PartPose.offset(0.375F, 8.6446F, 7.4875F));

		PartDefinition cube_r33 = Teeth32.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.725F, 0.3625F, -0.4375F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r34 = Teeth32.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-0.725F, 1.0875F, -0.6375F, -0.1872F, -0.1841F, 0.8027F));

		PartDefinition Teeth33 = Teeth.addOrReplaceChild("Teeth33", CubeListBuilder.create(), PartPose.offset(5.375F, 8.6446F, 7.4875F));

		PartDefinition cube_r35 = Teeth33.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.725F, 0.3625F, -0.4375F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r36 = Teeth33.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-0.725F, 1.0875F, -0.6375F, -0.1872F, -0.1841F, 0.8027F));

		PartDefinition Teeth34 = Teeth.addOrReplaceChild("Teeth34", CubeListBuilder.create(), PartPose.offset(8.375F, 9.2946F, 4.6375F));

		PartDefinition cube_r37 = Teeth34.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.725F, 0.3625F, -0.4375F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r38 = Teeth34.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-0.725F, 1.0875F, -0.6375F, -0.1872F, -0.1841F, 0.8027F));

		PartDefinition Teeth35 = Teeth.addOrReplaceChild("Teeth35", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.55F, 9.2946F, 6.2625F, 0.0F, 0.3491F, 0.0F));

		PartDefinition cube_r39 = Teeth35.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.725F, 0.3625F, -0.4375F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r40 = Teeth35.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-0.725F, 1.0875F, -0.6375F, -0.1872F, -0.1841F, 0.8027F));

		PartDefinition Teeth28 = Teeth.addOrReplaceChild("Teeth28", CubeListBuilder.create(), PartPose.offset(1.8186F, 9.1196F, -5.6519F));

		PartDefinition cube_r41 = Teeth28.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.0436F, -0.1125F, -0.3981F, 0.0F, 0.0873F, 0.0F));

		PartDefinition cube_r42 = Teeth28.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-0.0436F, 0.6125F, -0.5981F, 0.0618F, 0.0617F, 0.7873F));

		PartDefinition Teeth29 = Teeth.addOrReplaceChild("Teeth29", CubeListBuilder.create(), PartPose.offsetAndRotation(7.4436F, 9.7696F, -5.6519F, -0.0119F, -0.0732F, 0.1619F));

		PartDefinition cube_r43 = Teeth29.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.0436F, -0.1125F, -0.3981F, 0.0F, 0.0873F, 0.0F));

		PartDefinition cube_r44 = Teeth29.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-0.0436F, 0.6125F, -0.5981F, 0.0618F, 0.0617F, 0.7873F));

		PartDefinition Teeth30 = Teeth.addOrReplaceChild("Teeth30", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.9814F, 9.7696F, -5.6519F, 0.0139F, -0.0729F, -0.1881F));

		PartDefinition cube_r45 = Teeth30.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(4, 10).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-0.0436F, -0.1125F, -0.3981F, 0.0F, 0.0873F, 0.0F));

		PartDefinition cube_r46 = Teeth30.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(4, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-0.0436F, 0.6125F, -0.5981F, 0.0618F, 0.0617F, 0.7873F));

		PartDefinition Tendrils3 = jellyfish.addOrReplaceChild("Tendrils3", CubeListBuilder.create(), PartPose.offset(-10.2645F, 3.3222F, 8.775F));

		PartDefinition tendril_r1 = Tendrils3.addOrReplaceChild("tendril_r1", CubeListBuilder.create().texOffs(66, 28).mirror().addBox(0.0F, -2.4F, -1.2F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.1694F, 5.9172F, 0.05F, 0.0F, -1.5708F, 0.6109F));

		PartDefinition tendril_r2 = Tendrils3.addOrReplaceChild("tendril_r2", CubeListBuilder.create().texOffs(66, 6).mirror().addBox(-1.5F, -1.05F, -1.5F, 3.0F, 13.0F, 3.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7418F));

		PartDefinition LowerTendril3 = Tendrils3.addOrReplaceChild("LowerTendril3", CubeListBuilder.create(), PartPose.offset(-7.9047F, 8.6178F, 0.0F));

		PartDefinition tendril_r3 = LowerTendril3.addOrReplaceChild("tendril_r3", CubeListBuilder.create().texOffs(66, 28).mirror().addBox(0.0F, -2.475F, -1.175F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.2153F, 1.6478F, 0.05F, 0.0F, 1.5708F, -2.2253F));

		PartDefinition tendril_r4 = LowerTendril3.addOrReplaceChild("tendril_r4", CubeListBuilder.create().texOffs(78, 7).mirror().addBox(-1.5F, -0.425F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(-0.1179F, 0.1375F, -0.5F, 0.0F, 0.0F, 0.7418F));

		PartDefinition LowerTendril1Section7 = LowerTendril3.addOrReplaceChild("LowerTendril1Section7", CubeListBuilder.create().texOffs(90, 5).mirror().addBox(-2.65F, -5.9F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.2761F, 7.8258F, 0.0F));

		PartDefinition tendril_r5 = LowerTendril1Section7.addOrReplaceChild("tendril_r5", CubeListBuilder.create().texOffs(89, 23).mirror().addBox(-1.475F, -3.75F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-1.4857F, -6.5847F, 0.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition tendril_r6 = LowerTendril1Section7.addOrReplaceChild("tendril_r6", CubeListBuilder.create().texOffs(69, 36).mirror().addBox(-7.825F, -4.0F, 24.15F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.6648F, 28.1281F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition tendril_r7 = LowerTendril1Section7.addOrReplaceChild("tendril_r7", CubeListBuilder.create().texOffs(101, 22).mirror().addBox(-7.425F, -2.95F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.2369F, -10.2761F, 0.0F, -0.0618F, 0.0617F, -1.2673F));

		PartDefinition tendril_r8 = LowerTendril1Section7.addOrReplaceChild("tendril_r8", CubeListBuilder.create().texOffs(101, 22).mirror().addBox(-1.625F, -6.475F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.2369F, -10.2761F, 0.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition LowerTendril1Section8 = LowerTendril1Section7.addOrReplaceChild("LowerTendril1Section8", CubeListBuilder.create(), PartPose.offset(-1.2194F, 7.649F, 0.0F));

		PartDefinition LowerMiddleTendril = LowerTendril1Section8.addOrReplaceChild("LowerMiddleTendril", CubeListBuilder.create(), PartPose.offset(-1.1264F, 0.9061F, 0.0F));

		PartDefinition spike_r1 = LowerMiddleTendril.addOrReplaceChild("spike_r1", CubeListBuilder.create().texOffs(82, 29).mirror().addBox(0.0F, -3.0F, -16.025F, 0.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.6838F, 3.9503F, -4.6F, 3.1416F, 0.0F, -0.3054F));

		PartDefinition tendril_r9 = LowerMiddleTendril.addOrReplaceChild("tendril_r9", CubeListBuilder.create().texOffs(102, 5).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.1264F, -0.9061F, 0.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition tendril_r10 = LowerMiddleTendril.addOrReplaceChild("tendril_r10", CubeListBuilder.create().texOffs(69, 36).mirror().addBox(-1.55F, -4.0F, 7.8F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(69, 36).mirror().addBox(-1.55F, -4.0F, 16.95F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(8.0106F, 19.573F, 0.0F, 1.5708F, 0.0F, -0.3054F));

		PartDefinition LowerTendril1Section9 = LowerMiddleTendril.addOrReplaceChild("LowerTendril1Section9", CubeListBuilder.create(), PartPose.offset(5.4264F, 12.5439F, 0.0F));

		PartDefinition tendril_r11 = LowerTendril1Section9.addOrReplaceChild("tendril_r11", CubeListBuilder.create().texOffs(113, 22).mirror().addBox(-0.625F, -2.5F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.9674F, 9.7273F, -0.225F, -3.1416F, 0.0F, 1.2654F));

		PartDefinition tendril_r12 = LowerTendril1Section9.addOrReplaceChild("tendril_r12", CubeListBuilder.create().texOffs(113, 22).mirror().addBox(-2.0F, -1.025F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.1142F, 11.6568F, -0.225F, 0.0F, 0.0F, -0.3054F));

		PartDefinition tendril_r13 = LowerTendril1Section9.addOrReplaceChild("tendril_r13", CubeListBuilder.create().texOffs(69, 36).mirror().addBox(-1.55F, -4.0F, -1.175F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(69, 36).mirror().addBox(-1.55F, -4.0F, -6.425F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.5842F, 7.0291F, 0.0F, 1.5708F, 0.0F, -0.3054F));

		PartDefinition spike_r2 = LowerTendril1Section9.addOrReplaceChild("spike_r2", CubeListBuilder.create().texOffs(82, 29).mirror().addBox(-0.375F, 5.1F, 6.275F, 0.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.8136F, -6.8943F, -17.375F, 0.0F, 0.0F, -0.3054F));

		PartDefinition tendril_r14 = LowerTendril1Section9.addOrReplaceChild("tendril_r14", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.0901F, -0.098F, 0.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition Tendrils4 = jellyfish.addOrReplaceChild("Tendrils4", CubeListBuilder.create(), PartPose.offset(-10.2645F, 3.3222F, -8.7F));

		PartDefinition tendril_r15 = Tendrils4.addOrReplaceChild("tendril_r15", CubeListBuilder.create().texOffs(66, 28).mirror().addBox(0.0F, -2.4F, -1.2F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.1694F, 5.9172F, 0.05F, 0.0F, -1.5708F, 0.6109F));

		PartDefinition tendril_r16 = Tendrils4.addOrReplaceChild("tendril_r16", CubeListBuilder.create().texOffs(66, 6).mirror().addBox(-1.5F, -1.05F, -1.5F, 3.0F, 13.0F, 3.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7418F));

		PartDefinition LowerTendril4 = Tendrils4.addOrReplaceChild("LowerTendril4", CubeListBuilder.create(), PartPose.offset(-7.9047F, 8.6178F, 0.0F));

		PartDefinition tendril_r17 = LowerTendril4.addOrReplaceChild("tendril_r17", CubeListBuilder.create().texOffs(66, 28).mirror().addBox(0.0F, -2.475F, -1.175F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.2153F, 1.6478F, 0.05F, 0.0F, 1.5708F, -2.2253F));

		PartDefinition tendril_r18 = LowerTendril4.addOrReplaceChild("tendril_r18", CubeListBuilder.create().texOffs(78, 7).mirror().addBox(-1.5F, -0.425F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(-0.1179F, 0.1375F, -0.5F, 0.0F, 0.0F, 0.7418F));

		PartDefinition LowerTendril1Section5 = LowerTendril4.addOrReplaceChild("LowerTendril1Section5", CubeListBuilder.create().texOffs(90, 5).mirror().addBox(-2.65F, -5.9F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.2761F, 7.8258F, 0.0F));

		PartDefinition tendril_r19 = LowerTendril1Section5.addOrReplaceChild("tendril_r19", CubeListBuilder.create().texOffs(89, 23).mirror().addBox(-1.475F, -3.75F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-1.4857F, -6.5847F, 0.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition tendril_r20 = LowerTendril1Section5.addOrReplaceChild("tendril_r20", CubeListBuilder.create().texOffs(69, 36).mirror().addBox(-7.825F, -4.0F, 24.15F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.6648F, 28.1281F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition spike_r3 = LowerTendril1Section5.addOrReplaceChild("spike_r3", CubeListBuilder.create().texOffs(82, 29).addBox(0.0F, -3.0F, -15.8F, 0.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4474F, 2.5054F, -4.575F, 3.1416F, 0.0F, 0.0F));

		PartDefinition tendril_r21 = LowerTendril1Section5.addOrReplaceChild("tendril_r21", CubeListBuilder.create().texOffs(101, 22).mirror().addBox(-7.425F, -2.95F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.2369F, -10.2761F, 0.0F, -0.0618F, 0.0617F, -1.2673F));

		PartDefinition tendril_r22 = LowerTendril1Section5.addOrReplaceChild("tendril_r22", CubeListBuilder.create().texOffs(101, 22).mirror().addBox(-1.625F, -6.475F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.2369F, -10.2761F, 0.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition LowerTendril1Section6 = LowerTendril1Section5.addOrReplaceChild("LowerTendril1Section6", CubeListBuilder.create(), PartPose.offset(-1.2194F, 7.649F, 0.0F));

		PartDefinition MiddleTentacle = LowerTendril1Section6.addOrReplaceChild("MiddleTentacle", CubeListBuilder.create(), PartPose.offset(-0.8014F, 0.5311F, 0.0F));

		PartDefinition tendril_r23 = MiddleTentacle.addOrReplaceChild("tendril_r23", CubeListBuilder.create().texOffs(69, 36).mirror().addBox(-1.55F, -4.0F, 16.95F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(69, 36).mirror().addBox(-1.55F, -4.0F, 7.8F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.6856F, 19.948F, 0.0F, 1.5708F, 0.0F, -0.3054F));

		PartDefinition spike_r4 = MiddleTentacle.addOrReplaceChild("spike_r4", CubeListBuilder.create().texOffs(82, 29).mirror().addBox(-0.375F, -4.9F, -11.725F, 0.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.2878F, 6.0246F, 0.1F, 0.0F, 0.0F, -0.3054F));

		PartDefinition tendril_r24 = MiddleTentacle.addOrReplaceChild("tendril_r24", CubeListBuilder.create().texOffs(102, 5).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.8014F, -0.5311F, 0.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition LowerTendril1Section10 = MiddleTentacle.addOrReplaceChild("LowerTendril1Section10", CubeListBuilder.create(), PartPose.offset(5.1014F, 12.9189F, 0.0F));

		PartDefinition tendril_r25 = LowerTendril1Section10.addOrReplaceChild("tendril_r25", CubeListBuilder.create().texOffs(113, 22).mirror().addBox(-0.625F, -2.5F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.9674F, 9.7273F, -0.225F, -3.1416F, 0.0F, 1.2654F));

		PartDefinition tendril_r26 = LowerTendril1Section10.addOrReplaceChild("tendril_r26", CubeListBuilder.create().texOffs(113, 22).mirror().addBox(-2.0F, -1.025F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.1142F, 11.6568F, -0.225F, 0.0F, 0.0F, -0.3054F));

		PartDefinition tendril_r27 = LowerTendril1Section10.addOrReplaceChild("tendril_r27", CubeListBuilder.create().texOffs(69, 36).mirror().addBox(-1.55F, -4.0F, -1.175F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(69, 36).mirror().addBox(-1.55F, -4.0F, -6.425F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.5842F, 7.0291F, 0.0F, 1.5708F, 0.0F, -0.3054F));

		PartDefinition tendril_r28 = LowerTendril1Section10.addOrReplaceChild("tendril_r28", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.0901F, -0.098F, 0.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition Tendrils2 = jellyfish.addOrReplaceChild("Tendrils2", CubeListBuilder.create(), PartPose.offset(10.2645F, 3.3222F, 8.775F));

		PartDefinition tendril_r29 = Tendrils2.addOrReplaceChild("tendril_r29", CubeListBuilder.create().texOffs(66, 28).addBox(0.0F, -2.4F, -1.2F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.1694F, 5.9172F, 0.05F, 0.0F, 1.5708F, -0.6109F));

		PartDefinition tendril_r30 = Tendrils2.addOrReplaceChild("tendril_r30", CubeListBuilder.create().texOffs(66, 6).addBox(-1.5F, -1.05F, -1.5F, 3.0F, 13.0F, 3.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition LowerTendril2 = Tendrils2.addOrReplaceChild("LowerTendril2", CubeListBuilder.create(), PartPose.offset(7.9047F, 8.6178F, 0.0F));

		PartDefinition tendril_r31 = LowerTendril2.addOrReplaceChild("tendril_r31", CubeListBuilder.create().texOffs(66, 28).addBox(0.0F, -2.475F, -1.175F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2153F, 1.6478F, 0.05F, 0.0F, -1.5708F, 2.2253F));

		PartDefinition tendril_r32 = LowerTendril2.addOrReplaceChild("tendril_r32", CubeListBuilder.create().texOffs(78, 7).addBox(-1.5F, -0.425F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.1179F, 0.1375F, -0.5F, 0.0F, 0.0F, -0.7418F));

		PartDefinition LowerTendril1Section2 = LowerTendril2.addOrReplaceChild("LowerTendril1Section2", CubeListBuilder.create().texOffs(90, 5).addBox(-0.35F, -5.9F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.2761F, 7.8258F, 0.0F));

		PartDefinition tendril_r33 = LowerTendril1Section2.addOrReplaceChild("tendril_r33", CubeListBuilder.create().texOffs(89, 23).addBox(-1.525F, -3.75F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.4857F, -6.5847F, 0.0F, 0.0F, 0.0F, 0.3054F));

		PartDefinition tendril_r34 = LowerTendril1Section2.addOrReplaceChild("tendril_r34", CubeListBuilder.create().texOffs(69, 36).addBox(7.825F, -4.0F, 24.15F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6648F, 28.1281F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition spike_r5 = LowerTendril1Section2.addOrReplaceChild("spike_r5", CubeListBuilder.create().texOffs(82, 29).addBox(0.0F, -3.0F, -16.5F, 0.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.662F, 2.5054F, 4.95F, 0.0F, 0.0F, 0.0F));

		PartDefinition tendril_r35 = LowerTendril1Section2.addOrReplaceChild("tendril_r35", CubeListBuilder.create().texOffs(101, 22).addBox(1.425F, -2.95F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2369F, -10.2761F, 0.0F, -0.0618F, -0.0617F, 1.2673F));

		PartDefinition tendril_r36 = LowerTendril1Section2.addOrReplaceChild("tendril_r36", CubeListBuilder.create().texOffs(101, 22).addBox(-4.375F, -6.475F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2369F, -10.2761F, 0.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition LowerTendril1Section3 = LowerTendril1Section2.addOrReplaceChild("LowerTendril1Section3", CubeListBuilder.create(), PartPose.offset(1.2194F, 7.649F, 0.0F));

		PartDefinition LowerTendrilMiddle = LowerTendril1Section3.addOrReplaceChild("LowerTendrilMiddle", CubeListBuilder.create(), PartPose.offset(0.5764F, 0.3061F, 0.0F));

		PartDefinition tendril_r37 = LowerTendrilMiddle.addOrReplaceChild("tendril_r37", CubeListBuilder.create().texOffs(69, 36).addBox(1.55F, -4.0F, 16.95F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(69, 36).addBox(1.55F, -4.0F, 7.8F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.4606F, 20.173F, 0.0F, 1.5708F, 0.0F, 0.3054F));

		PartDefinition spike_r6 = LowerTendrilMiddle.addOrReplaceChild("spike_r6", CubeListBuilder.create().texOffs(82, 29).addBox(0.0F, -3.0F, -17.05F, 0.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1338F, 4.5503F, -5.6F, 3.1416F, 0.0F, 0.3054F));

		PartDefinition tendril_r38 = LowerTendrilMiddle.addOrReplaceChild("tendril_r38", CubeListBuilder.create().texOffs(102, 5).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5764F, -0.3061F, 0.0F, 0.0F, 0.0F, 0.3054F));

		PartDefinition LowerTendril1Section4 = LowerTendrilMiddle.addOrReplaceChild("LowerTendril1Section4", CubeListBuilder.create(), PartPose.offset(-4.8764F, 13.1439F, 0.0F));

		PartDefinition tendril_r39 = LowerTendril1Section4.addOrReplaceChild("tendril_r39", CubeListBuilder.create().texOffs(113, 22).addBox(-6.375F, -2.5F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9674F, 9.7273F, -0.225F, -3.1416F, 0.0F, -1.2654F));

		PartDefinition tendril_r40 = LowerTendril1Section4.addOrReplaceChild("tendril_r40", CubeListBuilder.create().texOffs(113, 22).addBox(-5.0F, -1.025F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.1142F, 11.6568F, -0.225F, 0.0F, 0.0F, 0.3054F));

		PartDefinition tendril_r41 = LowerTendril1Section4.addOrReplaceChild("tendril_r41", CubeListBuilder.create().texOffs(69, 36).addBox(1.55F, -4.0F, -1.175F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(69, 36).addBox(1.55F, -4.0F, -6.425F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5842F, 7.0291F, 0.0F, 1.5708F, 0.0F, 0.3054F));

		PartDefinition tendril_r42 = LowerTendril1Section4.addOrReplaceChild("tendril_r42", CubeListBuilder.create().texOffs(114, 5).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0901F, -0.098F, 0.0F, 0.0F, 0.0F, 0.3054F));

		PartDefinition Tendrils5 = jellyfish.addOrReplaceChild("Tendrils5", CubeListBuilder.create(), PartPose.offset(10.2645F, 3.3222F, -8.75F));

		PartDefinition tendril_r43 = Tendrils5.addOrReplaceChild("tendril_r43", CubeListBuilder.create().texOffs(66, 28).addBox(0.0F, -2.4F, -1.2F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.1694F, 5.9172F, 0.05F, 0.0F, 1.5708F, -0.6109F));

		PartDefinition tendril_r44 = Tendrils5.addOrReplaceChild("tendril_r44", CubeListBuilder.create().texOffs(66, 6).addBox(-1.5F, -1.05F, -1.5F, 3.0F, 13.0F, 3.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition LowerTendril5 = Tendrils5.addOrReplaceChild("LowerTendril5", CubeListBuilder.create(), PartPose.offset(7.9047F, 8.6178F, 0.0F));

		PartDefinition tendril_r45 = LowerTendril5.addOrReplaceChild("tendril_r45", CubeListBuilder.create().texOffs(66, 28).addBox(0.0F, -2.475F, -1.175F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2153F, 1.6478F, 0.05F, 0.0F, -1.5708F, 2.2253F));

		PartDefinition tendril_r46 = LowerTendril5.addOrReplaceChild("tendril_r46", CubeListBuilder.create().texOffs(78, 7).addBox(-1.5F, -0.425F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.1179F, 0.1375F, -0.5F, 0.0F, 0.0F, -0.7418F));

		PartDefinition LowerTendril1Section11 = LowerTendril5.addOrReplaceChild("LowerTendril1Section11", CubeListBuilder.create().texOffs(90, 5).addBox(-0.35F, -5.9F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.2761F, 7.8258F, 0.0F));

		PartDefinition tendril_r47 = LowerTendril1Section11.addOrReplaceChild("tendril_r47", CubeListBuilder.create().texOffs(89, 23).addBox(-1.525F, -3.75F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.4857F, -6.5847F, 0.0F, 0.0F, 0.0F, 0.3054F));

		PartDefinition tendril_r48 = LowerTendril1Section11.addOrReplaceChild("tendril_r48", CubeListBuilder.create().texOffs(69, 36).addBox(7.825F, -4.0F, 24.15F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6648F, 28.1281F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition tendril_r49 = LowerTendril1Section11.addOrReplaceChild("tendril_r49", CubeListBuilder.create().texOffs(101, 22).addBox(1.425F, -2.95F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2369F, -10.2761F, 0.0F, -0.0618F, -0.0617F, 1.2673F));

		PartDefinition tendril_r50 = LowerTendril1Section11.addOrReplaceChild("tendril_r50", CubeListBuilder.create().texOffs(101, 22).addBox(-4.375F, -6.475F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2369F, -10.2761F, 0.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition LowerTendril1Section12 = LowerTendril1Section11.addOrReplaceChild("LowerTendril1Section12", CubeListBuilder.create(), PartPose.offset(2.5547F, 8.2092F, 0.125F));

		PartDefinition LowerTendrilSectionMiddle = LowerTendril1Section12.addOrReplaceChild("LowerTendrilSectionMiddle", CubeListBuilder.create(), PartPose.offset(-0.5589F, -0.0791F, -0.125F));

		PartDefinition tendril_r51 = LowerTendrilSectionMiddle.addOrReplaceChild("tendril_r51", CubeListBuilder.create().texOffs(69, 36).addBox(1.55F, -4.0F, 16.95F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(69, 36).addBox(1.55F, -4.0F, 7.8F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.6606F, 19.998F, 0.0F, 1.5708F, 0.0F, 0.3054F));

		PartDefinition spike_r7 = LowerTendrilSectionMiddle.addOrReplaceChild("spike_r7", CubeListBuilder.create().texOffs(82, 29).addBox(0.375F, -4.9F, -11.725F, 0.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.2628F, 6.0746F, 0.15F, 0.0F, 0.0F, 0.3054F));

		PartDefinition tendril_r52 = LowerTendrilSectionMiddle.addOrReplaceChild("tendril_r52", CubeListBuilder.create().texOffs(102, 5).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7764F, -0.4811F, 0.0F, 0.0F, 0.0F, 0.3054F));

		PartDefinition LowerTendril1Section13 = LowerTendrilSectionMiddle.addOrReplaceChild("LowerTendril1Section13", CubeListBuilder.create(), PartPose.offset(-5.0764F, 12.9689F, 0.0F));

		PartDefinition tendril_r53 = LowerTendril1Section13.addOrReplaceChild("tendril_r53", CubeListBuilder.create().texOffs(113, 22).addBox(-6.375F, -2.5F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9674F, 9.7273F, -0.225F, -3.1416F, 0.0F, -1.2654F));

		PartDefinition tendril_r54 = LowerTendril1Section13.addOrReplaceChild("tendril_r54", CubeListBuilder.create().texOffs(113, 22).addBox(-5.0F, -1.025F, 0.0F, 7.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.1142F, 11.6568F, -0.225F, 0.0F, 0.0F, 0.3054F));

		PartDefinition tendril_r55 = LowerTendril1Section13.addOrReplaceChild("tendril_r55", CubeListBuilder.create().texOffs(69, 36).addBox(1.55F, -4.0F, -1.175F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(69, 36).addBox(1.55F, -4.0F, -6.425F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5842F, 7.0291F, 0.0F, 1.5708F, 0.0F, 0.3054F));

		PartDefinition spike_r8 = LowerTendril1Section13.addOrReplaceChild("spike_r8", CubeListBuilder.create().texOffs(82, 29).addBox(0.0F, -15.0F, 0.95F, 0.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7426F, -8.5936F, 11.925F, 3.1416F, 0.0F, 0.3054F));

		PartDefinition tendril_r56 = LowerTendril1Section13.addOrReplaceChild("tendril_r56", CubeListBuilder.create().texOffs(114, 5).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0901F, -0.098F, 0.0F, 0.0F, 0.0F, 0.3054F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.reset();

		this.animate(entity.idle, JellyfishAnim.idle,ageInTicks);

		this.animate(entity.idleGround, JellyfishAnim.idleground,ageInTicks);

		this.animate(entity.idleJump, JellyfishAnim.air,ageInTicks);

		this.animate(entity.jump, JellyfishAnim.jump,ageInTicks);

		this.animate(entity.startSummoning,JellyfishAnim.startsummon,ageInTicks);

		this.animate(entity.summoning,JellyfishAnim.summoning,ageInTicks);

		this.animate(entity.startLazer,JellyfishAnim.startlazer,ageInTicks);

		this.animate(entity.attackGround,JellyfishAnim.attackhead,ageInTicks);

		this.animate(entity.shootLaser,JellyfishAnim.shootlazer,ageInTicks);

		if(entity.startLazerTimer>0 || entity.lazerTimer>0){
			this.truemain.xRot = (float) (headPitch*Math.PI/180.0F) + (float)Math.toRadians(3.5F);
		}
		this.truemain.y+=40;

		this.animate(entity.spinning,JellyfishAnim.spinning,ageInTicks);
	}

	public void reset(){
		this.root().getAllParts().forEach(ModelPart::resetPose);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		truemain.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}


	@Override
	public ModelPart root() {
		return this.truemain;
	}
}