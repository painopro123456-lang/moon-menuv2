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
	private static final int ROW_HEIGHT = 66;
	private static final int ROW_GAP = 6;
	private static final int GRID_COLUMNS = 4;
	private static final int RAIL_WIDTH = 0;
	private static final int FOOTER_HEIGHT = 26;

	private static final int PANEL_BG = 0x42FFFFFF;
	private static final int RAIL_BG = 0x42FFFFFF;
	private static final int ROW_HOVER = 0x42FFFFFF;
	private static final int ROW_ALT = 0x42FFFFFF;

	private static final String[] MODULE_ICONS = {
			"beacon",
			"tinted_glass",
			"ink_sac",
			"compass",
			"spyglass",
			"chain",
			"clock",
			"map",
			"netherrack",
			"recovery_compass",
			"feather",
			"grass_block",
			"ender_eye",
			"daylight_detector",
			"cobblestone",
			"torch",
			"redstone",
			"pig_spawn_egg",
			"ender_pearl",
			"golden_apple",
			"cooked_beef",
			"diamond_chestplate",
			"oak_sign",
			"target",
			"totem_of_undying",
			"item_frame",
			"experience_bottle",
			"leather_boots",
			"turtle_helmet",
			"ender_chest",
			"stone",
			"writable_book",
			"nether_star",
			"potion",
			"slime_ball",
			"iron_sword",
			"white_stained_glass_pane",
			"repeater",
			"comparator",
			"rabbit_foot",
			"observer",
			"redstone_lamp",
			"white_wool",
			"paper",
			"water_bucket",
			"oak_leaves",
			"black_dye",
			"glass",
			"glowstone",
			"amethyst_shard",
			"piston",
			"coal",
			"book",
			"prismarine_crystals",
			"end_rod",
			"lightning_rod",
			"firework_rocket",
			"tripwire_hook",
			"minecart",
			"arrow",
			"spectral_arrow",
			"flint",
			"lever",
			"rabbit_hide",
			"golden_boots",
			"shield",
			"lectern",
			"string",
			"cyan_dye",
			"chainmail_helmet",
			"bundle",
			"lodestone",
			"amethyst_cluster",
			"barrier",
			"chest",
			"anvil",
			"crafting_table",
			"furnace",
			"blast_furnace",
			"smoker",
			"enchanting_table",
			"brewing_stand",
			"cauldron",
			"hopper",
			"dispenser",
			"dropper",
			"sculk_sensor",
			"note_block",
			"jukebox",
			"bell",
			"lantern",
			"soul_lantern",
			"campfire",
			"soul_campfire",
			"bucket",
			"lava_bucket",
			"snowball",
			"egg",
			"fishing_rod",
			"bow",
			"crossbow",
			"trident",
			"iron_helmet"
	};

	private static Category lastCategory = Category.HUD;

	private final Map<String, Float> hoverAnim = new HashMap<>();
	private final Map<String, Float> knobAnim = new HashMap<>();
	private final Map<String, AbstractWidget> sliders = new LinkedHashMap<>();
	private final List<Row> rows = new ArrayList<>();
	private final List<Feature> visible = new ArrayList<>();

	private Category category = Category.HUD;
	private String query = "";
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
	private int searchHeight = 18;

	private record Row(Feature feature, int y, int x, int width,
			int controlX, int controlY, int controlWidth, int controlHeight) {
	}

	public VloxScreen() {
		super(Component.literal("Moon Menu"));
	}

	@Override
	protected void init() {
		this.panelWidth = Math.min(760, this.width - 32);
		this.panelHeight = Math.min(440, this.height - 32);
		this.panelX = (this.width - this.panelWidth) / 2;
		this.panelY = (this.height - this.panelHeight) / 2;

		this.contentX = this.panelX;
		this.contentWidth = this.panelWidth - RAIL_WIDTH;
		this.listY = this.panelY + 82;
		this.listHeight = this.panelHeight - 92 - FOOTER_HEIGHT;

		this.searchX = this.contentX + 16;
		this.searchY = this.panelY + 48;
		this.searchWidth = Math.min(300, this.contentWidth - 32);

		if (!VloxConfig.flag(Features.MENU_REMEMBER)) {
			lastCategory = Category.HUD;
		}
		if (this.category == null) {
			this.category = lastCategory;
		}

		refresh();
	}

	private void refresh() {
		visible.clear();
		String needle = query.trim().toLowerCase();

		for (Feature feature : Features.ALL) {
			if (!needle.isEmpty()
					&& !feature.label.toLowerCase().contains(needle)
					&& !feature.id.toLowerCase().contains(needle)
					&& !feature.description.toLowerCase().contains(needle)) {
				continue;
			}
			visible.add(feature);
		}

		float maxScroll = Math.max(0, ((visible.size() + GRID_COLUMNS - 1) / GRID_COLUMNS) * (ROW_HEIGHT + ROW_GAP) - listHeight + 8);
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
			int y = Math.round(this.listY + 6 + gridRow * (ROW_HEIGHT + ROW_GAP) - this.scrollShown);

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

			int controlX = x + (cardWidth - controlWidth) / 2;
			int controlY = y + ROW_HEIGHT - controlHeight - 7;
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

		// No fullscreen dimmer: Minecraft remains visible around and through the menu.

		Ui.panel(graphics, this.panelX, this.panelY, this.panelWidth, this.panelHeight, 14, PANEL_BG);

		// Side rail
		Ui.rounded(graphics, this.panelX, this.panelY, RAIL_WIDTH, this.panelHeight, 10, RAIL_BG);
		graphics.fill(this.panelX + RAIL_WIDTH - 1, this.panelY + 12, this.panelX + RAIL_WIDTH,
				this.panelY + this.panelHeight - 12, 0x42FFFFFF);

		drawRail(graphics, mouseX, mouseY);
		drawHeader(graphics);
		drawRows(graphics, mouseX, mouseY, dt);
		drawFooter(graphics, mouseX, mouseY);
		drawScrollbar(graphics);

		positionSliders();

		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	private void drawRail(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int x = this.panelX + 18;
		Ui.textScaled(graphics, this.font, "MOON MENU", x, this.panelY + 16, Ui.accent(), 1.75F);
		Ui.text(graphics, this.font, "MODS", x + 1, this.panelY + 34, Ui.FAINT);
		Ui.text(graphics, this.font, "v2.0.0",
				this.panelX + this.panelWidth - 52, this.panelY + 22, Ui.FAINT);
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
		Ui.text(graphics, this.font, "All Modules", this.contentX + 16, this.panelY + 22, Ui.TEXT);

		String count = this.visible.size() + " modules";
		Ui.text(graphics, this.font, count,
				this.contentX + this.contentWidth - 16 - this.font.width(count),
				this.panelY + 23, Ui.MUTED);

		Ui.rounded(graphics, this.searchX, this.searchY, this.searchWidth, this.searchHeight, 9, 0x42FFFFFF);
		graphics.fill(this.searchX + 9, this.searchY + 4, this.searchX + 10, this.searchY + 14, Ui.FAINT);

		String shown = this.query.isEmpty() ? "Search settings" : this.query;
		Ui.text(graphics, this.font, Ui.fit(this.font, shown, this.searchWidth - 26),
				this.searchX + 16, this.searchY + 6, this.query.isEmpty() ? Ui.FAINT : Ui.TEXT);

		if (!this.query.isEmpty() && (System.currentTimeMillis() / 500L) % 2L == 0L) {
			int caretX = this.searchX + 16 + Math.min(this.font.width(this.query), this.searchWidth - 26);
			graphics.fill(caretX, this.searchY + 5, caretX + 1, this.searchY + 14, Ui.accent());
		}

		graphics.fill(this.contentX + 16, this.listY - 6, this.contentX + this.contentWidth - 16,
				this.listY - 5, 0x42FFFFFF);
	}

	private void drawRows(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float dt) {
		int top = this.listY;
		int bottom = this.listY + this.listHeight;
		for (Row row : rows) {
			if (row.y() + ROW_HEIGHT < top - 2 || row.y() > bottom + 2) continue;
			Feature feature = row.feature();
			boolean hovered = Ui.inside(mouseX, mouseY, row.x(), row.y(), row.width(), ROW_HEIGHT);
			float target = hovered ? 1.0F : 0.0F;
			float current = hoverAnim.getOrDefault(feature.id, 0.0F);
			float next = Ui.lerp(current, target, dt * 14.0F);
			hoverAnim.put(feature.id, next);
			Ui.rounded(graphics, row.x(), row.y(), row.width(), ROW_HEIGHT - 2, 9, 0x42FFFFFF);
			if (next > 0.03F) {
				graphics.fill(row.x(), row.y() + 9, row.x() + 2, row.y() + ROW_HEIGHT - 11, Ui.alpha(Ui.accent(), (int) (220 * next)));
			}
			int iconIndex = Features.ALL.indexOf(feature);
			if (iconIndex >= 0 && iconIndex < MODULE_ICONS.length) {
				ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", MODULE_ICONS[iconIndex])));
				graphics.item(icon, row.x() + (row.width() - 16) / 2, row.y() + 5);
			}
			String label = Ui.fit(this.font, feature.label, row.width() - 12);
			int labelX = row.x() + (row.width() - this.font.width(label)) / 2;
			Ui.text(graphics, this.font, label, labelX, row.y() + 25, Ui.TEXT);
			if (VloxConfig.flag(Features.MENU_DESCRIPTIONS) && !VloxConfig.flag(Features.MENU_COMPACT)) {
				String description = Ui.fit(this.font, feature.description, row.width() - 12);
				int descriptionX = row.x() + (row.width() - this.font.width(description)) / 2;
				Ui.text(graphics, this.font, description, descriptionX, row.y() + 36, Ui.MUTED);
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
		int track = on ? Ui.alpha(Ui.accent(), 90) : 0x42FFFFFF;

		Ui.rounded(graphics, x, y, width, height, 8, track);
		if (on) {
			Ui.rounded(graphics, x, y, width, height, 8, Ui.alpha(Ui.accent(), 60));
		}

		int knobX = x + 3 + Math.round(current * (width - 6 - 10));
		Ui.rounded(graphics, knobX, y + 3, 10, 10, 5, on ? Ui.accent() : 0xFF6C7A8C);
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

		graphics.fill(this.contentX + 16, y, this.contentX + this.contentWidth - 16, y + 1, 0x42FFFFFF);

		Feature hoveredFeature = null;
		for (Row row : rows) {
			if (Ui.inside(mouseX, mouseY, row.x(), row.y(), row.width(), ROW_HEIGHT - 2)) {
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
					this.contentX + 16, y + 9, Ui.accent());
		} else {
			String hint = hoveredFeature != null ? hoveredFeature.description
					: "Hover a setting to see what it does";
			Ui.text(graphics, this.font, Ui.fit(this.font, hint, this.contentWidth - 140),
					this.contentX + 16, y + 9, hoveredFeature != null ? Ui.MUTED : Ui.FAINT);
		}

		String close = "Right Shift to close";
		Ui.text(graphics, this.font, close,
				this.contentX + this.contentWidth - 16 - this.font.width(close), y + 9, Ui.FAINT);
	}

	private void drawScrollbar(GuiGraphicsExtractor graphics) {
		int trackX = this.contentX + this.contentWidth - 7;
		int trackTop = this.listY + 4;
		int trackHeight = this.listHeight - 8;
		int total = ((this.visible.size() + GRID_COLUMNS - 1) / GRID_COLUMNS) * (ROW_HEIGHT + ROW_GAP);
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

			boolean inside = row.y() + ROW_HEIGHT > this.listY && row.y() < this.listY + this.listHeight;
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

		for (Row row : rows) {
			if (!Ui.inside(mouseX, mouseY, row.x(), row.y(), row.width(), ROW_HEIGHT - 2)) {
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

		float total = ((this.visible.size() + GRID_COLUMNS - 1) / GRID_COLUMNS) * (ROW_HEIGHT + ROW_GAP);
		float max = Math.max(0, total - this.listHeight + 8);
		this.scrollTarget = Math.max(0, Math.min(max, this.scrollTarget - (float) vertical * 34.0F));
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == GLFW.GLFW_KEY_BACKSPACE && !this.query.isEmpty()) {
			this.query = this.query.substring(0, this.query.length() - 1);
			refresh();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (event.isAllowedChatCharacter() && this.query.length() < 24) {
			this.query += event.codepointAsString();
			refresh();
			return true;
		}
		return super.charTyped(event);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}
}
