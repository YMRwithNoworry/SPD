package software.bernie.geckolib.renderer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

import javax.annotation.Nullable;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import java.util.List;

/**
 * Base interface for all GeckoLib renderers.<br>
 */
public interface GeoRenderer<T extends GeoAnimatable> {
	/**
	 * Gets the model instance for this renderer
	 */
	GeoModel<T> getGeoModel();

	/**
	 * Gets the {@link GeoAnimatable} instance currently being rendered
	 */
	T getAnimatable();

	/**
	 * Gets the texture resource location to render for the given animatable
	 */
	default class_2960 getTextureLocation(T animatable) {
		return getGeoModel().getTextureResource(animatable);
	}

	/**
	 * Returns the list of registered {@link GeoRenderLayer GeoRenderLayers} for this renderer
	 */
	default List<GeoRenderLayer<T>> getRenderLayers() {
		return List.of();
	}

	/**
	 * Gets the {@link class_1921} to render the given animatable with.<br>
	 * Uses the {@link class_1921#method_23578}	{@code RenderType} by default.<br>
	 * Override this to change the way a model will render (such as translucent models, etc)
	 */
	default class_1921 getRenderType(T animatable, class_2960 texture,
									 @Nullable class_4597 bufferSource,
									 float partialTick) {
		return getGeoModel().getRenderType(animatable, texture);
	}

	/**
	 * Gets a tint-applying color to render the given animatable with.<br>
	 * Returns {@link Color#WHITE} by default
	 */
	default Color getRenderColor(T animatable, float partialTick, int packedLight) {
		return Color.WHITE;
	}

	/**
	 * Gets a packed overlay coordinate pair for rendering.<br>
	 * Mostly just used for the red tint when an entity is hurt,
	 * but can be used for other things like the {@link net.minecraft.class_1548}
	 * white tint when exploding.
	 * @deprecated Use {@link GeoRenderer#getPackedOverlay(GeoAnimatable, float, float)}
	 */
	@Deprecated(forRemoval = true)
	default int getPackedOverlay(T animatable, float u) {
		return class_4608.field_21444;
	}

	/**
	 * Gets a packed overlay coordinate pair for rendering.<br>
	 * Mostly just used for the red tint when an entity is hurt,
	 * but can be used for other things like the {@link net.minecraft.class_1548}
	 * white tint when exploding.
	 */
	default int getPackedOverlay(T animatable, float u, float partialTick) {
		return getPackedOverlay(animatable, u);
	}

	/**
	 * Gets the id that represents the current animatable's instance for animation purposes.
	 * This is mostly useful for things like items, which have a single registered instance for all objects
	 */
	default long getInstanceId(T animatable) {
		return animatable.hashCode();
	}

	/**
	 * Determines the threshold value before the animatable should be considered moving for animation purposes.<br>
	 * The default value and usage for this varies depending on the renderer.<br>
	 * <ul>
	 *     <li>For entities, it represents the averaged lateral velocity of the object.</li>
	 *     <li>For {@link software.bernie.geckolib.animatable.GeoBlockEntity Tile Entities} and {@link software.bernie.geckolib.animatable.GeoItem Items}, it's currently unused</li>
	 * </ul>
	 * The lower the value, the more sensitive the {@link AnimationState#isMoving()} check will be.<br>
	 * Particularly low values may have adverse effects however
	 */
	default float getMotionAnimThreshold(T animatable) {
		return 0.015f;
	}

	/**
	 * Initial access point for rendering. It all begins here.<br>
	 * All GeckoLib renderers should immediately defer their respective default {@code render} calls to this, for consistent handling
	 */
	default void defaultRender(class_4587 poseStack, T animatable, class_4597 bufferSource, @Nullable class_1921 renderType, @Nullable class_4588 buffer,
							   float yaw, float partialTick, int packedLight) {
		poseStack.method_22903();

		Color renderColor = getRenderColor(animatable, partialTick, packedLight);
		float red = renderColor.getRedFloat();
		float green = renderColor.getGreenFloat();
		float blue = renderColor.getBlueFloat();
		float alpha = renderColor.getAlphaFloat();
		int packedOverlay = getPackedOverlay(animatable, 0, partialTick);
		BakedGeoModel model = getGeoModel().getBakedModel(getGeoModel().getModelResource(animatable));

		if (renderType == null)
			renderType = getRenderType(animatable, getTextureLocation(animatable), bufferSource, partialTick);

		if (buffer == null)
			buffer = bufferSource.getBuffer(renderType);

		preRender(poseStack, animatable, model, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

		if (firePreRenderEvent(poseStack, model, bufferSource, partialTick, packedLight)) {
			preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, packedLight, packedLight, packedOverlay);
			actuallyRender(poseStack, animatable, model, renderType,
					bufferSource, buffer, false, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
			applyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
			postRender(poseStack, animatable, model, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
			firePostRenderEvent(poseStack, model, bufferSource, partialTick, packedLight);
		}

		poseStack.method_22909();

		renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		doPostRenderCleanup();
	}

	/**
	 * Re-renders the provided {@link BakedGeoModel} using the existing {@link GeoRenderer}.<br>
	 * Usually you'd use this for rendering alternate {@link class_1921} layers or for sub-model rendering whilst inside a {@link GeoRenderLayer} or similar
	 */
	default void reRender(BakedGeoModel model, class_4587 poseStack, class_4597 bufferSource, T animatable,
						  class_1921 renderType, class_4588 buffer, float partialTick,
						  int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		poseStack.method_22903();
		preRender(poseStack, animatable, model, bufferSource, buffer, true, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, true, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		postRender(poseStack, animatable, model, bufferSource, buffer, true, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.method_22909();
	}

	/**
	 * The actual render method that sub-type renderers should override to handle their specific rendering tasks.<br>
	 * {@link GeoRenderer#preRender} has already been called by this stage, and {@link GeoRenderer#postRender} will be called directly after
	 */
	default void actuallyRender(class_4587 poseStack, T animatable, BakedGeoModel model, class_1921 renderType,
								class_4597 bufferSource, class_4588 buffer, boolean isReRender, float partialTick,
								int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		updateAnimatedTextureFrame(animatable);

		for (GeoBone group : model.topLevelBones()) {
			renderRecursively(poseStack, animatable, group, renderType, bufferSource, buffer, isReRender, partialTick, packedLight,
					packedOverlay, red, green, blue, alpha);
		}
	}

	/**
	 * Calls back to the various {@link GeoRenderLayer RenderLayers} that have been registered to this renderer for their {@link GeoRenderLayer#preRender pre-render} actions.
	 */
	default void preApplyRenderLayers(class_4587 poseStack, T animatable, BakedGeoModel model, class_1921 renderType, class_4597 bufferSource,
								   class_4588 buffer, float partialTick, int packedLight, int packedOverlay) {
		for (GeoRenderLayer<T> renderLayer : getRenderLayers()) {
			renderLayer.preRender(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
		}
	}

	/**
	 * Calls back to the various {@link GeoRenderLayer RenderLayers} that have been registered to this renderer for their {@link GeoRenderLayer#renderForBone per-bone} render actions.
	 */
	default void applyRenderLayersForBone(class_4587 poseStack, T animatable, GeoBone bone, class_1921 renderType, class_4597 bufferSource,
										  class_4588 buffer, float partialTick, int packedLight, int packedOverlay) {
		for (GeoRenderLayer<T> renderLayer : getRenderLayers()) {
			renderLayer.renderForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
		}
	}

	/**
	 * Render the various {@link GeoRenderLayer RenderLayers} that have been registered to this renderer
	 */
	default void applyRenderLayers(class_4587 poseStack, T animatable, BakedGeoModel model, class_1921 renderType, class_4597 bufferSource,
								   class_4588 buffer, float partialTick, int packedLight, int packedOverlay) {
		for (GeoRenderLayer<T> renderLayer : getRenderLayers()) {
			renderLayer.render(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
		}
	}

	/**
	 * Called before rendering the model to buffer. Allows for render modifications and preparatory
	 * work such as scaling and translating.<br>
	 * {@link class_4587} translations made here are kept until the end of the render process
	 */
	default void preRender(class_4587 poseStack, T animatable, BakedGeoModel model, @Nullable class_4597 bufferSource, @Nullable class_4588 buffer, boolean isReRender, float partialTick, int packedLight,
						   int packedOverlay, float red, float green, float blue, float alpha) {}

	/**
	 * Called after rendering the model to buffer. Post-render modifications should be performed here.<br>
	 * {@link class_4587} transformations will be unused and lost once this method ends
	 */
	default void postRender(class_4587 poseStack, T animatable, BakedGeoModel model, class_4597 bufferSource, class_4588 buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue,
							float alpha) {}

	/**
	 * Call after all other rendering work has taken place, including reverting the {@link class_4587}'s state. This method is <u>not</u> called in {@link GeoRenderer#reRender re-render}
	 */
	default void renderFinal(class_4587 poseStack, T animatable, BakedGeoModel model, class_4597 bufferSource, class_4588 buffer, float partialTick, int packedLight,
							 int packedOverlay, float red, float green, float blue, float alpha) {}

	/**
	 * Called after all render operations are completed and the render pass is considered functionally complete.
	 * <p>
	 * Use this method to clean up any leftover persistent objects stored during rendering or any other post-render maintenance tasks as required
	 */
	default void doPostRenderCleanup() {}

	/**
	 * Renders the provided {@link GeoBone} and its associated child bones
	 */
	default void renderRecursively(class_4587 poseStack, T animatable, GeoBone bone, class_1921 renderType, class_4597 bufferSource,
								   class_4588 buffer, boolean isReRender, float partialTick, int packedLight,
								   int packedOverlay, float red, float green, float blue, float alpha) {
		poseStack.method_22903();
		RenderUtils.prepMatrixForBone(poseStack, bone);
		renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);

		if (!isReRender)
			applyRenderLayersForBone(poseStack, getAnimatable(), bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

		renderChildBones(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.method_22909();
	}

	/**
	 * Renders the {@link GeoCube GeoCubes} associated with a given {@link GeoBone}
	 */
	default void renderCubesOfBone(class_4587 poseStack, GeoBone bone, class_4588 buffer, int packedLight,
								   int packedOverlay, float red, float green, float blue, float alpha) {
		if (bone.isHidden())
			return;

		for (GeoCube cube : bone.getCubes()) {
			poseStack.method_22903();
			renderCube(poseStack, cube, buffer, packedLight, packedOverlay, red, green, blue, alpha);
			poseStack.method_22909();
		}
	}

	/**
	 * Render the child bones of a given {@link GeoBone}.<br>
	 * Note that this does not render the bone itself. That should be done through {@link GeoRenderer#renderCubesOfBone} separately
	 */
	default void renderChildBones(class_4587 poseStack, T animatable, GeoBone bone, class_1921 renderType, class_4597 bufferSource, class_4588 buffer,
								  boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (bone.isHidingChildren())
			return;

		for (GeoBone childBone : bone.getChildBones()) {
			renderRecursively(poseStack, animatable, childBone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		}
	}

	/**
	 * Renders an individual {@link GeoCube}.<br>
	 * This tends to be called recursively from something like {@link GeoRenderer#renderCubesOfBone}
	 */
	default void renderCube(class_4587 poseStack, GeoCube cube, class_4588 buffer, int packedLight,
							int packedOverlay, float red, float green, float blue, float alpha) {
		RenderUtils.translateToPivotPoint(poseStack, cube);
		RenderUtils.rotateMatrixAroundCube(poseStack, cube);
		RenderUtils.translateAwayFromPivotPoint(poseStack, cube);

		Matrix3f normalisedPoseState = poseStack.method_23760().method_23762();
		Matrix4f poseState = new Matrix4f(poseStack.method_23760().method_23761());

		for (GeoQuad quad : cube.quads()) {
			if (quad == null)
				continue;

			Vector3f normal = normalisedPoseState.transform(new Vector3f(quad.normal()));
			
			RenderUtils.fixInvertedFlatCube(cube, normal);
			createVerticesOfQuad(quad, poseState, normal, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		}
	}

	/**
	 * Applies the {@link GeoQuad Quad's} {@link GeoVertex vertices} to the given {@link class_4588 buffer} for rendering
	 */
	default void createVerticesOfQuad(GeoQuad quad, Matrix4f poseState, Vector3f normal, class_4588 buffer,
			int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		for (GeoVertex vertex : quad.vertices()) {
			Vector3f position = vertex.position();			
			Vector4f vector4f = poseState.transform(new Vector4f(position.x(), position.y(), position.z(), 1.0f));

			buffer.method_23919(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha, vertex.texU(),
					vertex.texV(), packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
		}
	}

	/**
	 * Create and fire the relevant {@code CompileLayers} event hook for this renderer
	 */
	void fireCompileRenderLayersEvent();

	/**
	 * Create and fire the relevant {@code Pre-Render} event hook for this renderer.<br>
	 * @return Whether the renderer should proceed based on the cancellation state of the event
	 */
	boolean firePreRenderEvent(class_4587 poseStack, BakedGeoModel model, class_4597 bufferSource, float partialTick, int packedLight);

	/**
	 * Create and fire the relevant {@code Post-Render} event hook for this renderer
	 */
	void firePostRenderEvent(class_4587 poseStack, BakedGeoModel model, class_4597 bufferSource, float partialTick, int packedLight);
	
    /**
     * Scales the {@link class_4587} in preparation for rendering the model, excluding when re-rendering the model as part of a {@link GeoRenderLayer} or external render call.<br>
     * Override and call super with modified scale values as needed to further modify the scale of the model (E.G. child entities)
     */
	default void scaleModelForRender(float widthScale, float heightScale, class_4587 poseStack, T animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
		if (!isReRender && (widthScale != 1 || heightScale != 1))
			poseStack.method_22905(widthScale, heightScale, widthScale);
	}

	/**
	 * Update the current frame of a {@link AnimatableTexture potentially animated} texture used by this GeoRenderer.<br>
	 * This should only be called immediately prior to rendering, and only
	 * @see AnimatableTexture#setAndUpdate
	 */
	void updateAnimatedTextureFrame(T animatable);
}
