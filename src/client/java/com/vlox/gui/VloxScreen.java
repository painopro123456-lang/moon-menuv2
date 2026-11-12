package com.vlox.gui;

import com.vlox.config.Presets;
import com.vlox.config.VloxConfig;
import com.vlox.feature.Category;
import com.vlox.feature.Feature;
import com.vlox.feature.Features;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The VLOX menu, drawn by hand. */
public final class VloxScreen extends Screen {
	private static final int ROW_HEIGHT = 84;
	private int cardHeight() {
		return VloxConfig.flag(Features.MENU_COMPACT) ? 54 : ROW_HEIGHT;
	}
	private static final int ROW_GAP = 10;
	private static final int GRID_COLUMNS = 4;
	private static final int RAIL_WIDTH = 154;
	private static final int FOOTER_HEIGHT = 32;

	private static final int GLASS = 0x38FFFFFF;
	private static final int PANEL_BG = 0x24FFFFFF;
	private static final int RAIL_BG = 0x24FFFFFF;
	private static final int ROW_HOVER = 0x48FFFFFF;
	private static final int ROW_ALT = 0x30FFFFFF;

	private static final Map<String, String> MODULE_ICONS = Map.ofEntries(
			Map.entry("hud.enabled", "beacon"),
			Map.entry("hud.background", "tinted_glass"),
			Map.entry("hud.shadow", "ink_sac"),
			Map.entry("hud.corner", "compass"),
			Map.entry("hud.scale", "spyglass"),
			Map.entry("hud.spacing", "chain"),
			Map.entry("hud.fps", "clock"),
			Map.entry("hud.coords", "map"),
			Map.entry("hud.nether", "netherrack"),
			Map.entry("hud.facing", "recovery_compass"),
			Map.entry("hud.speed", "feather"),
			Map.entry("hud.biome", "grass_block"),
			Map.entry("hud.dimension", "ender_eye"),
			Map.entry("hud.time", "daylight_detector"),
			Map.entry("hud.day", "sunflower"),
			Map.entry("hud.chunk", "lodestone"),
			Map.entry("hud.light", "torch"),
			Map.entry("hud.memory", "amethyst_shard"),
			Map.entry("hud.entities", "pig_spawn_egg"),
			Map.entry("hud.ping", "ender_pearl"),
			Map.entry("hud.health", "golden_apple"),
			Map.entry("hud.hunger", "cooked_beef"),
			Map.entry("hud.armor", "diamond_chestplate"),
			Map.entry("hud.keystrokes", "target"),
			Map.entry("hud.cps", "target"),
			Map.entry("hud.items", "totem_of_undying"),
			Map.entry("hud.hand", "item_frame"),
			Map.entry("hud.xp", "experience_bottle"),
			Map.entry("hud.sprint", "feather"),
			Map.entry("hud.sneak", "leather_boots"),
			Map.entry("hud.looking", "spyglass"),
			Map.entry("hud.blockid", "writable_book"),
			Map.entry("hud.playtime", "clock"),
			Map.entry("hud.clock", "clock"),
			Map.entry("hud.server", "oak_sign"),
			Map.entry("hud.effects", "potion"),
			Map.entry("hud.fall", "rabbit_foot"),
			Map.entry("hud.difficulty", "iron_sword"),
			Map.entry("hud.deathPoint", "recovery_compass"),
			Map.entry("hud.backdropOpacity", "glass"),
			Map.entry("video.renderDistance", "spyglass"),
			Map.entry("video.simulationDistance", "observer"),
			Map.entry("video.entityDistance", "ender_pearl"),
			Map.entry("video.fpsLimit", "clock"),
			Map.entry("video.vsync", "repeater"),
			Map.entry("video.clouds", "white_wool"),
			Map.entry("video.cloudRange", "glass"),
			Map.entry("video.weatherRadius", "water_bucket"),
			Map.entry("video.cutoutLeaves", "oak_leaves"),
			Map.entry("video.vignette", "black_dye"),
			Map.entry("video.improvedTransparency", "glass"),
			Map.entry("video.ambientOcclusion", "glowstone"),
			Map.entry("video.chunkFade", "piston"),
			Map.entry("video.chunkUpdates", "anvil"),
			Map.entry("video.entityShadows", "chainmail_helmet"),
			Map.entry("video.mipmaps", "book"),
			Map.entry("video.biomeBlend", "grass_block"),
			Map.entry("video.anisotropy", "spyglass"),
			Map.entry("video.menuBlur", "tinted_glass"),
			Map.entry("video.panoramaSpeed", "clock"),
			Map.entry("video.highContrast", "black_dye"),
			Map.entry("video.highContrastOutline", "target"),
			Map.entry("video.hideLightning", "lightning_rod"),
			Map.entry("video.hideSplash", "barrier"),
			Map.entry("input.sensitivity", "spyglass"),
			Map.entry("input.mouseWheel", "minecart"),
			Map.entry("input.invertX", "arrow"),
			Map.entry("input.invertY", "spectral_arrow"),
			Map.entry("input.raw", "feather"),
			Map.entry("input.discreteScroll", "bundle"),
			Map.entry("input.cursorChanges", "feather"),
			Map.entry("input.autoJump", "rabbit_foot"),
			Map.entry("input.minecartRotate", "minecart"),
			Map.entry("input.mainHand", "shield"),
			Map.entry("input.suggestions", "writable_book"),
			Map.entry("input.autoSprint", "feather"),
			Map.entry("input.zoom", "spyglass"),
			Map.entry("input.zoomAmount", "spyglass"),
			Map.entry("input.zoomSmooth", "clock"),
			Map.entry("chat.visibility", "writable_book"),
			Map.entry("chat.opacity", "tinted_glass"),
			Map.entry("chat.scale", "book"),
			Map.entry("chat.lineSpacing", "chain"),
			Map.entry("chat.width", "paper"),
			Map.entry("chat.heightFocused", "book"),
			Map.entry("chat.heightUnfocused", "paper"),
			Map.entry("chat.colors", "cyan_dye"),
			Map.entry("chat.links", "chain"),
			Map.entry("chat.backgroundOnly", "glass"),
			Map.entry("chat.delay", "clock"),
			Map.entry("chat.textBackground", "glass"),
			Map.entry("menu.descriptions", "writable_book"),
			Map.entry("menu.compact", "bundle"),
			Map.entry("menu.showFps", "clock"),
			Map.entry("menu.rememberSearch", "book"),
			Map.entry("menu.searchFocus", "spyglass"),
			Map.entry("menu.accent", "cyan_dye"),
			Map.entry("menu.presetPvp", "shield"),
			Map.entry("menu.presetExplorer", "compass"),
			Map.entry("menu.presetPerformance", "anvil"),
			Map.entry("menu.reset", "barrier"),
			Map.entry("menu.copyCoords", "map"),
			Map.entry("menu.copyPath", "paper")
	);

	private static Category lastCategory = Category.HUD;

	private final Map<String, Float> hoverAnim = new HashMap<>();
	private final Map<String, Float> knobAnim = new HashMap<>();
	private final Map<String, AbstractWidget> sliders = new LinkedHashMap<>();
	private final List<Row> rows = new ArrayList<>();
	private final List<Feature> visible = new ArrayList<>();

	private Category category = Category.HUD;
	private String query = "";
	private boolean searchFocused;
	private float scrollTarget;
	private float scrollShown;
	private long lastFrame = System.nanoTime();
	private String status;
	private long statusAt;

	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int contentX;
	private int contentWidth;
	private int listY;
	private int listHeight;
	private int searchX;
	private int searchY;
	private int searchWidth;
	private int searchHeight = 24;

	private record Row(Feature feature, int y, int x, int width,
			int controlX, int controlY, int controlWidth, int controlHeight) {
	}

	public VloxScreen() {
		super(Component.literal("Moon Menu"));
	}

	@Override
	protected void init() {
		this.panelWidth = Math.min(840, this.width - 40);
		this.panelHeight = Math.min(520, this.height - 40);
		this.panelX = (this.width - this.panelWidth) / 2;
		this.panelY = (this.height - this.panelHeight) / 2;

		this.contentX = this.panelX + RAIL_WIDTH;
		this.contentWidth = this.panelWidth - RAIL_WIDTH;
		this.listY = this.panelY + 126;
		this.listHeight = this.panelHeight - 126 - FOOTER_HEIGHT;

		this.searchWidth = Math.min(280, this.contentWidth - 32);
		this.searchX = this.contentX + this.contentWidth - this.searchWidth - 18;
		this.searchY = this.panelY + 64;

		this.searchFocused = VloxConfig.flag(Features.MENU_SEARCH_FOCUS);
		this.query = VloxConfig.flag(Features.MENU_REMEMBER)
				? VloxConfig.getText("menu.lastSearch", "") : "";
		if (this.category == null) {
			this.category = lastCategory;
		}

		refresh();
	}

	private void refresh() {
		visible.clear();
		String needle = query.trim().toLowerCase();

		for (Feature feature : Features.ALL) {
			if (feature.category != category) continue;
			if (!needle.isEmpty()
					&& !feature.label.toLowerCase().contains(needle)
					&& !feature.id.toLowerCase().contains(needle)
					&& !feature.description.toLowerCase().contains(needle)) {
				continue;
			}
			visible.add(feature);
		}

		float maxScroll = Math.max(0, ((visible.size() + GRID_COLUMNS - 1) / GRID_COLUMNS) * (cardHeight() + ROW_GAP) - listHeight + 8);
		scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget));

		clearWidgets();
		sliders.clear();

		for (Feature feature : visible) {
			OptionInstance<?> option = Features.optionFor(feature);
			if (option != null && !isBooleanOption(option) && !isEnumOption(option)) {
				AbstractWidget widget = option.createButton(this.minecraft.options,
						this.contentX + 16, this.listY, 130);
				widget.setHeight(18);
				sliders.put(feature.id, widget);
				addRenderableWidget(widget);
			}
		}
	}

	private static boolean isBooleanOption(OptionInstance<?> option) {
		return option.get() instanceof Boolean;
	}

	private static boolean isEnumOption(OptionInstance<?> option) {
		return option.get() instanceof Enum<?>;
	}

	// -------------------------------------------------------------------------
	// Layout for the current frame
	// -------------------------------------------------------------------------

	private void buildRows() {
		rows.clear();
		int availableWidth = this.contentWidth - 32;
		int cardWidth = (availableWidth - (GRID_COLUMNS - 1) * ROW_GAP) / GRID_COLUMNS;

		for (int i = 0; i < visible.size(); i++) {
			Feature feature = visible.get(i);
			int column = i % GRID_COLUMNS;
			int gridRow = i / GRID_COLUMNS;
			int x = this.contentX + 16 + column * (cardWidth + ROW_GAP);
			int y = Math.round(this.listY + 6 + gridRow * (cardHeight() + ROW_GAP) - this.scrollShown);

			int controlWidth = Math.min(92, cardWidth - 12);
			int controlHeight = 18;
			OptionInstance<?> option = Features.optionFor(feature);

			if (option != null) {
				controlWidth = isBooleanOption(option) ? 34 : isEnumOption(option) ? Math.min(92, cardWidth - 12) : Math.min(92, cardWidth - 12);
				controlHeight = isBooleanOption(option) ? 16 : 18;
			} else {
				switch (feature.type) {
					case TOGGLE -> {
						controlWidth = 34;
						controlHeight = 16;
					}
					case NUMBER -> {
						controlWidth = Math.min(92, cardWidth - 12);
						controlHeight = 18;
					}
					case CHOICE -> {
						controlWidth = Math.min(92, cardWidth - 12);
						controlHeight = 18;
					}
					case ACTION -> {
						controlWidth = 64;
						controlHeight = 18;
					}
				}
			}

			boolean toggleControl = (option != null && isBooleanOption(option)) || (option == null && feature.type == Feature.Type.TOGGLE);
			int controlX = x + (cardWidth - controlWidth) / 2;
			int controlY = y + cardHeight() - controlHeight - 9;
			rows.add(new Row(feature, y, x, cardWidth, controlX, controlY, controlWidth, controlHeight));
		}
	}

	// -------------------------------------------------------------------------
	// Drawing
	// -------------------------------------------------------------------------

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		long now = System.nanoTime();
		float dt = Math.min(0.1F, (now - this.lastFrame) / 1_000_000_000.0F);
		this.lastFrame = now;

		this.scrollShown = Ui.lerp(this.scrollShown, this.scrollTarget, dt * 16.0F);
		if (Math.abs(this.scrollShown - this.scrollTarget) < 0.4F) {
			this.scrollShown = this.scrollTarget;
		}

		buildRows();

		// Keep the world visible around and through the floating glass window.
		Ui.panel(graphics, this.panelX, this.panelY, this.panelWidth, this.panelHeight, 16, PANEL_BG);
		drawPanelBorder(graphics);

		drawRail(graphics, mouseX, mouseY);
		drawHeader(graphics);
		drawRows(graphics, mouseX, mouseY, dt);
		drawFooter(graphics, mouseX, mouseY);
		drawScrollbar(graphics);

		positionSliders();

		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	private void drawPanelBorder(GuiGraphicsExtractor graphics) {
		int x = this.panelX;
		int y = this.panelY;
		int right = x + this.panelWidth;
		int bottom = y + this.panelHeight;
		graphics.fill(x + 16, y, right - 16, y + 1, 0x70FFFFFF);
		graphics.fill(x + 16, bottom - 1, right - 16, bottom, 0x70FFFFFF);
		graphics.fill(x, y + 16, x + 1, bottom - 16, 0x70FFFFFF);
		graphics.fill(right - 1, y + 16, right, bottom - 16, 0x70FFFFFF);
	}

	private void drawRail(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int x = this.panelX + 18;
		// Brand block: a compact glass tile with a strong wordmark.
		Ui.rounded(graphics, x, this.panelY + 18, 34, 34, 11, 0x6AFFFFFF);
		graphics.fill(x + 7, this.panelY + 24, x + 27, this.panelY + 25, 0xBFFFFFFF);
		Ui.textScaled(graphics, this.font, "M", x + 11, this.panelY + 27, Ui.TEXT, 1.25F);
		Ui.textScaled(graphics, this.font, "MOON", x + 43, this.panelY + 20, Ui.TEXT, 1.18F);
		Ui.text(graphics, this.font, "CLIENT  /  V2", x + 44, this.panelY + 39, Ui.MUTED);

		graphics.fill(this.panelX + RAIL_WIDTH - 1, this.panelY + 16, this.panelX + RAIL_WIDTH,
				this.panelY + this.panelHeight - 16, 0x42FFFFFF);

		Ui.text(graphics, this.font, "WORKSPACE", this.panelX + 22, this.panelY + 78, Ui.FAINT);
		int navY = this.panelY + 99;
		for (Category cat : Category.values()) {
			boolean selected = this.category == cat;
			boolean hovered = Ui.inside(mouseX, mouseY, this.panelX + 12, navY - 5, RAIL_WIDTH - 24, 34);
			if (selected) {
				Ui.rounded(graphics, this.panelX + 12, navY - 5, RAIL_WIDTH - 24, 34, 10, 0x72FFFFFF);
				graphics.fill(this.panelX + 12, navY + 4, this.panelX + 14, navY + 20, 0xFF526F91);
			} else if (hovered) {
				Ui.rounded(graphics, this.panelX + 12, navY - 5, RAIL_WIDTH - 24, 34, 10, 0x32FFFFFF);
			}
			Ui.text(graphics, this.font, cat.label, this.panelX + 27, navY + 7,
					selected ? 0xFF172638 : Ui.MUTED);
			String count = Integer.toString(countIn(cat));
			Ui.rounded(graphics, this.panelX + RAIL_WIDTH - 39, navY + 3, 22, 18, 7,
					selected ? 0x55FFFFFF : 0x20FFFFFF);
			Ui.text(graphics, this.font, count, this.panelX + RAIL_WIDTH - 28 - this.font.width(count) / 2,
					navY + 8, selected ? Ui.TEXT : Ui.FAINT);
			navY += 41;
		}

		int footerY = this.panelY + this.panelHeight - 48;
		graphics.fill(this.panelX + 18, footerY - 9, this.panelX + RAIL_WIDTH - 18, footerY - 8, 0x35FFFFFF);
		Ui.text(graphics, this.font, "MOON MENU V2", this.panelX + 20, footerY + 1, Ui.MUTED);
		Ui.text(graphics, this.font, "FABRIC 26.2", this.panelX + 20, footerY + 15, Ui.FAINT);
	}

	private int countIn(Category cat) {
		int total = 0;
		for (Feature feature : Features.ALL) {
			if (feature.category == cat) {
				total++;
			}
		}
		return total;
	}

	private void drawHeader(GuiGraphicsExtractor graphics) {
		Ui.textScaled(graphics, this.font, "MODULE LIBRARY", this.contentX + 20,
				this.panelY + 48, Ui.TEXT, 1.12F);
		String count = this.visible.size() + " modules in " + this.category.label;
		if (VloxConfig.flag(Features.MENU_SHOW_FPS)) {
			count += "   /   " + Minecraft.getInstance().getFps() + " FPS";
		}
		Ui.text(graphics, this.font, count, this.contentX + 20, this.panelY + 68, Ui.MUTED);

		Ui.rounded(graphics, this.searchX - 1, this.searchY - 1, this.searchWidth + 2, 26, 10, 0x30000000);
		Ui.rounded(graphics, this.searchX, this.searchY, this.searchWidth, 24, 9, 0x48FFFFFF);
		// Minimal magnifier glyph.
		graphics.fill(this.searchX + 12, this.searchY + 7, this.searchX + 14, this.searchY + 15, 0xFF65778A);
		graphics.fill(this.searchX + 10, this.searchY + 9, this.searchX + 16, this.searchY + 11, 0xFF65778A);
		String shown = this.query.isEmpty() ? "Find a module..." : this.query;
		Ui.text(graphics, this.font, Ui.fit(this.font, shown, this.searchWidth - 34), this.searchX + 23,
				this.searchY + 8, this.query.isEmpty() ? Ui.MUTED : Ui.TEXT);
		if (this.searchFocused && (System.currentTimeMillis() / 500L) % 2L == 0L) {
			int caretX = this.searchX + 23 + Math.min(this.font.width(this.query), this.searchWidth - 34);
			graphics.fill(caretX, this.searchY + 7, caretX + 1, this.searchY + 18, Ui.TEXT);
		}
		graphics.fill(this.contentX + 20, this.listY - 9, this.contentX + this.contentWidth - 20,
				this.listY - 8, 0x45FFFFFF);
	}

	private void drawRows(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float dt) {
		int top = this.listY;
		int bottom = this.listY + this.listHeight;
		for (Row row : rows) {
			if (row.y() + cardHeight() < top - 2 || row.y() > bottom + 2) continue;
			Feature feature = row.feature();
			boolean hovered = Ui.inside(mouseX, mouseY, row.x(), row.y(), row.width(), cardHeight());
			float target = hovered ? 1.0F : 0.0F;
			float current = hoverAnim.getOrDefault(feature.id, 0.0F);
			float next = Ui.lerp(current, target, dt * 14.0F);
			hoverAnim.put(feature.id, next);

			OptionInstance<?> option = Features.optionFor(feature);
			boolean enabled = option != null && option.get() instanceof Boolean value
					? value : option == null && feature.type == Feature.Type.TOGGLE && VloxConfig.flag(feature);

			// Layered shadow + frosted glass surface; every card follows the same neutral system.
			Ui.rounded(graphics, row.x() + 1, row.y() + 3, row.width(), cardHeight() - 2, 11, 0x30000000);
			Ui.rounded(graphics, row.x(), row.y(), row.width(), cardHeight() - 5, 11,
					hovered ? 0x55FFFFFF : 0x38FFFFFF);
			graphics.fill(row.x() + 11, row.y() + 1, row.x() + row.width() - 11, row.y() + 2, 0x55FFFFFF);
			graphics.fill(row.x() + 10, row.y() + cardHeight() - 7, row.x() + row.width() - 10,
					row.y() + cardHeight() - 6, 0x24FFFFFF);
			if (hovered && next > 0.02F) {
				Ui.rounded(graphics, row.x(), row.y(), row.width(), cardHeight() - 5, 11,
						Ui.alpha(0xFFFFFFFF, (int) (18 * next)));
			}

			String iconId = MODULE_ICONS.get(feature.id);
			if (iconId != null) {
				int badgeX = row.x() + (row.width() - 34) / 2;
				Ui.rounded(graphics, badgeX + 1, row.y() + 8, 34, 34, 11, 0x28000000);
				Ui.rounded(graphics, badgeX, row.y() + 7, 34, 34, 11,
						enabled ? 0x68FFFFFF : 0x42FFFFFF);
				ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", iconId)));
				graphics.item(icon, badgeX + 9, row.y() + 16);
			}

			String label = Ui.fit(this.font, feature.label, row.width() - 14);
			int labelX = row.x() + (row.width() - this.font.width(label)) / 2;
			Ui.text(graphics, this.font, label, labelX, row.y() + 45, Ui.TEXT);
			if (VloxConfig.flag(Features.MENU_DESCRIPTIONS) && !VloxConfig.flag(Features.MENU_COMPACT)) {
				String description = Ui.fit(this.font, feature.description, row.width() - 16);
				int descX = row.x() + (row.width() - this.font.width(description)) / 2;
				Ui.text(graphics, this.font, description, descX, row.y() + 58, Ui.MUTED);
			}

			drawControl(graphics, row, mouseX, mouseY, dt);
		}
	}

	private void drawControl(GuiGraphicsExtractor graphics, Row row, int mouseX, int mouseY, float dt) {
		Feature feature = row.feature();
		OptionInstance<?> option = Features.optionFor(feature);

		if (option != null) {
			if (isBooleanOption(option)) {
				drawSwitch(graphics, feature.id, row.controlX(), row.controlY(), Boolean.TRUE.equals(option.get()));
			} else if (isEnumOption(option)) {
				drawChip(graphics, row.controlX(), row.controlY(), row.controlWidth(), row.controlHeight(),
						optionLabel(option), hovered(mouseX, mouseY, row), false);
			}
			// Numeric options are handled by the vanilla slider widget.
			return;
		}

		switch (feature.type) {
			case TOGGLE -> drawSwitch(graphics, feature.id, row.controlX(), row.controlY(), VloxConfig.flag(feature));
			case CHOICE -> drawChip(graphics, row.controlX(), row.controlY(), row.controlWidth(), row.controlHeight(),
					feature.describeValue(VloxConfig.get(feature)), hovered(mouseX, mouseY, row), false);
			case NUMBER -> drawStepper(graphics, row, feature, mouseX, mouseY);
			case ACTION -> drawChip(graphics, row.controlX(), row.controlY(), row.controlWidth(), row.controlHeight(),
					"Run", hovered(mouseX, mouseY, row), true);
		}
	}

	private boolean hovered(int mouseX, int mouseY, Row row) {
		return Ui.inside(mouseX, mouseY, row.controlX(), row.controlY(), row.controlWidth(), row.controlHeight());
	}

	private void drawSwitch(GuiGraphicsExtractor graphics, String id, int x, int y, boolean on) {
		float target = on ? 1.0F : 0.0F;
		float current = knobAnim.getOrDefault(id, target);
		current = Ui.lerp(current, target, 0.25F);
		knobAnim.put(id, current);

		int width = 34;
		int height = 16;
		int track = GLASS;

		Ui.rounded(graphics, x, y, width, height, 8, track);


		int knobX = x + 3 + Math.round(current * (width - 6 - 10));
		Ui.rounded(graphics, knobX, y + 3, 10, 10, 5, on ? 0xFFFFFFFF : 0xFF6C7A8C);
	}

	private void drawChip(GuiGraphicsExtractor graphics, int x, int y, int width, int height, String text,
			boolean hovered, boolean accent) {
		int bg = 0x42FFFFFF;
		Ui.rounded(graphics, x, y, width, height, 7, bg);
		if (hovered) {
			Ui.rounded(graphics, x, y, width, height, 7, 0x42FFFFFF);
		}

		String shown = Ui.fit(this.font, text, width - 12);
		int textX = x + (width - this.font.width(shown)) / 2;
		Ui.text(graphics, this.font, shown, textX, y + (height - 8) / 2, accent ? Ui.accent() : Ui.TEXT);
	}

	private void drawStepper(GuiGraphicsExtractor graphics, Row row, Feature feature, int mouseX, int mouseY) {
		int x = row.controlX();
		int y = row.controlY();
		int width = row.controlWidth();
		int height = row.controlHeight();

		Ui.rounded(graphics, x, y, width, height, 5, 0x42FFFFFF);

		boolean hoverMinus = Ui.inside(mouseX, mouseY, x, y, 22, height);
		boolean hoverPlus = Ui.inside(mouseX, mouseY, x + width - 22, y, 22, height);

		if (hoverMinus) {
			Ui.rounded(graphics, x, y, 22, height, 5, 0x42FFFFFF);
		}
		if (hoverPlus) {
			Ui.rounded(graphics, x + width - 22, y, 22, height, 5, 0x42FFFFFF);
		}

		Ui.text(graphics, this.font, "-", x + 9, y + 5, hoverMinus ? Ui.accent() : Ui.MUTED);
		Ui.text(graphics, this.font, "+", x + width - 13, y + 5, hoverPlus ? Ui.accent() : Ui.MUTED);

		String value = feature.describeValue(VloxConfig.get(feature));
		Ui.text(graphics, this.font, value, x + (width - this.font.width(value)) / 2, y + 5, Ui.TEXT);
	}

	private void drawFooter(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int y = this.panelY + this.panelHeight - FOOTER_HEIGHT;

		graphics.fill(this.contentX + 20, y, this.contentX + this.contentWidth - 20, y + 1, 0x50FFFFFF);

		Feature hoveredFeature = null;
		for (Row row : rows) {
			if (Ui.inside(mouseX, mouseY, row.x(), row.y(), row.width(), cardHeight() - 2)) {
				hoveredFeature = row.feature();
				break;
			}
		}

		boolean showingStatus = this.status != null
				&& System.currentTimeMillis() - this.statusAt < 2200L;
		if (!showingStatus) {
			this.status = null;
		}

		if (showingStatus) {
			Ui.text(graphics, this.font, Ui.fit(this.font, this.status, this.contentWidth - 140),
					this.contentX + 20, y + 11, Ui.TEXT);
		} else {
			String hint = hoveredFeature != null ? hoveredFeature.description
					: "Hover a setting to see what it does";
			Ui.text(graphics, this.font, Ui.fit(this.font, hint, this.contentWidth - 140),
					this.contentX + 20, y + 11, hoveredFeature != null ? Ui.MUTED : Ui.FAINT);
		}

		String close = "Right Shift to close";
		Ui.text(graphics, this.font, close,
				this.contentX + this.contentWidth - 20 - this.font.width(close), y + 11, Ui.FAINT);
	}

	private void drawScrollbar(GuiGraphicsExtractor graphics) {
		int trackX = this.contentX + this.contentWidth - 7;
		int trackTop = this.listY + 4;
		int trackHeight = this.listHeight - 8;
		int total = ((this.visible.size() + GRID_COLUMNS - 1) / GRID_COLUMNS) * (cardHeight() + ROW_GAP);
		if (total <= this.listHeight) {
			return;
		}

		Ui.rounded(graphics, trackX, trackTop, 3, trackHeight, 2, 0x42FFFFFF);

		float ratio = (float) this.listHeight / total;
		int thumbHeight = Math.max(20, Math.round(trackHeight * ratio));
		float progress = this.scrollShown / Math.max(1, total - this.listHeight);
		int thumbY = trackTop + Math.round((trackHeight - thumbHeight) * Math.max(0, Math.min(1, progress)));

		Ui.rounded(graphics, trackX, thumbY, 3, thumbHeight, 2, 0x42FFFFFF);
	}

	private void positionSliders() {
		for (Row row : rows) {
			AbstractWidget widget = sliders.get(row.feature().id);
			if (widget == null) {
				continue;
			}

			boolean inside = row.y() + cardHeight() > this.listY && row.y() < this.listY + this.listHeight;
			widget.visible = inside;
			if (inside) {
				widget.setX(row.controlX());
				widget.setY(row.controlY());
				widget.setWidth(row.controlWidth());
			}
		}
	}

	// -------------------------------------------------------------------------
	// Input
	// -------------------------------------------------------------------------

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		double mouseX = event.x();
		double mouseY = event.y();

		int navY = this.panelY + 94;
		for (Category cat : Category.values()) {
			if (Ui.inside(mouseX, mouseY, this.panelX + 12, navY - 5, RAIL_WIDTH - 24, 31)) {
				this.category = cat;
				lastCategory = cat;
				this.scrollTarget = 0;
				this.scrollShown = 0;
				refresh();
				return true;
			}
			navY += 39;
		}

		if (Ui.inside(mouseX, mouseY, this.searchX, this.searchY, this.searchWidth, this.searchHeight)) {
			this.searchFocused = true;
			return true;
		}
		this.searchFocused = false;

		for (Row row : rows) {
			if (!Ui.inside(mouseX, mouseY, row.x(), row.y(), row.width(), cardHeight() - 2)) {
				continue;
			}

			if (applyClick(row, mouseX, mouseY)) {
				return true;
			}
			break;
		}

		return super.mouseClicked(event, doubled);
	}

	private boolean applyClick(Row row, double mouseX, double mouseY) {
		Feature feature = row.feature();
		OptionInstance<?> option = Features.optionFor(feature);

		if (option != null) {
			if (isBooleanOption(option) && Ui.inside(mouseX, mouseY, row.controlX(), row.controlY(),
					row.controlWidth(), row.controlHeight())) {
				toggleOption(option);
				return true;
			}
			if (isEnumOption(option) && Ui.inside(mouseX, mouseY, row.controlX(), row.controlY(),
					row.controlWidth(), row.controlHeight())) {
				cycleOptionEnum(option);
				return true;
			}
			return false;
		}

		boolean onControl = Ui.inside(mouseX, mouseY, row.controlX(), row.controlY(),
				row.controlWidth(), row.controlHeight());

		switch (feature.type) {
			case TOGGLE -> {
				if (onControl) {
					VloxConfig.setFlag(feature, !VloxConfig.flag(feature));
					return true;
				}
			}
			case CHOICE -> {
				if (onControl) {
					VloxConfig.set(feature, (VloxConfig.index(feature) + 1) % feature.choices.length);
					return true;
				}
			}
			case NUMBER -> {
				if (Ui.inside(mouseX, mouseY, row.controlX(), row.controlY(), 22, row.controlHeight())) {
					VloxConfig.nudge(feature, -feature.step);
					return true;
				}
				if (Ui.inside(mouseX, mouseY, row.controlX() + row.controlWidth() - 22, row.controlY(),
						22, row.controlHeight())) {
					VloxConfig.nudge(feature, feature.step);
					return true;
				}
			}
			case ACTION -> {
				if (onControl) {
					runAction(feature);
					return true;
				}
			}
		}

		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void toggleOption(OptionInstance<?> option) {
		Object value = option.get();
		if (value instanceof Boolean flag) {
			((OptionInstance) option).set(!flag);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void cycleOptionEnum(OptionInstance<?> option) {
		Object value = option.get();
		if (value instanceof Enum<?> constant) {
			Enum<?>[] constants = constant.getDeclaringClass().getEnumConstants();
			((OptionInstance) option).set(constants[(constant.ordinal() + 1) % constants.length]);
		}
	}

	private static String optionLabel(OptionInstance<?> option) {
		Object value = option.get();
		if (value instanceof Boolean flag) {
			return flag ? "On" : "Off";
		}
		if (value instanceof Enum<?> constant) {
			return Ui.pretty(constant.name());
		}
		return String.valueOf(value);
	}

	private void runAction(Feature feature) {
		if (feature == Features.MENU_RESET) {
			VloxConfig.resetAll();
			refresh();
			status("Every setting is back to its default");
			return;
		}
		if (feature == Features.MENU_COPY_PATH) {
			copy(VloxConfig.file().toAbsolutePath().toString(), "Config path copied");
			return;
		}
		if (feature == Features.MENU_COPY_COORDS) {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft == null || minecraft.player == null) {
				status("No world loaded");
				return;
			}
			var player = minecraft.player;
			String coords = Math.round(player.getX()) + " " + Math.round(player.getY()) + " " + Math.round(player.getZ());
			copy(coords, "Copied " + coords);
			return;
		}
		if (feature == Features.MENU_PRESET_PVP) {
			Presets.pvp();
			refresh();
			status("PvP layout applied");
			return;
		}
		if (feature == Features.MENU_PRESET_EXPLORER) {
			Presets.explorer();
			refresh();
			status("Explorer layout applied");
			return;
		}
		if (feature == Features.MENU_PRESET_PERFORMANCE) {
			Presets.performance();
			refresh();
			status("Performance options applied to options.txt");
		}
	}

	private void copy(String value, String message) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null && minecraft.keyboardHandler != null) {
			minecraft.keyboardHandler.setClipboard(value);
			status(message);
		}
	}

	private void status(String message) {
		this.status = message;
		this.statusAt = System.currentTimeMillis();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (vertical == 0) {
			return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
		}

		float total = ((this.visible.size() + GRID_COLUMNS - 1) / GRID_COLUMNS) * (cardHeight() + ROW_GAP);
		float max = Math.max(0, total - this.listHeight + 8);
		this.scrollTarget = Math.max(0, Math.min(max, this.scrollTarget - (float) vertical * 34.0F));
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.searchFocused && event.key() == GLFW.GLFW_KEY_BACKSPACE && !this.query.isEmpty()) {
			this.query = this.query.substring(0, this.query.length() - 1);
			rememberSearch();
			refresh();
			return true;
		}
		if (this.searchFocused && event.key() == GLFW.GLFW_KEY_ESCAPE) {
			this.searchFocused = false;
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (this.searchFocused && event.isAllowedChatCharacter() && this.query.length() < 48) {
			this.query += event.codepointAsString();
			rememberSearch();
			refresh();
			return true;
		}
		return super.charTyped(event);
	}

	private void rememberSearch() {
		if (VloxConfig.flag(Features.MENU_REMEMBER)) {
			VloxConfig.putText("menu.lastSearch", this.query);
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}
}
