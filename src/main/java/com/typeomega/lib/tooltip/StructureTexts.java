package com.typeomega.lib.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.typeomega.lib.lang.Tr;

import java.util.List;
import java.util.Map;

/** Texts of the structure section (D-430), worded like the GTNH MultiblockTooltipBuilder. */
public final class StructureTexts {

    static final Tr DIMENSIONS = Tr.of("typeomegalib.tooltip.structure.dimensions",
            "Dimensions: %s (Width x Height x Depth)", "Dimensões: %s (Largura x Altura x Profundidade)",
            "尺寸：%s（宽×高×深）");
    static final Tr STRUCTURE = Tr.of("typeomegalib.tooltip.structure.header", "Structure:", "Estrutura:", "结构：");
    static final Tr CONTROLLER = Tr.of("typeomegalib.tooltip.structure.controller", "Controller: %s",
            "Controlador: %s", "控制器：%s");
    static final Tr TIERED = Tr.of("typeomegalib.tooltip.structure.tiered", "(Tiered)", "(Por tier)", "（分级）");
    static final Tr ANY = Tr.of("typeomegalib.tooltip.structure.any", "Any %s", "Qualquer %s", "任意%s");
    static final Tr OWN_POSITIONS = Tr.of("typeomegalib.tooltip.structure.own_positions", "Its own positions",
            "Posições próprias", "专用位置");
    static final Tr OR = Tr.of("typeomegalib.tooltip.list.or", "%s or %s", "%s ou %s", "%s或%s");
    static final Tr POLLUTION = Tr.of("typeomegalib.tooltip.pollution", "Causes %s Pollution per second",
            "Causa %s de Poluição por segundo", "每秒产生%s污染");
    static final Tr CHANNEL = Tr.of("typeomegalib.tooltip.structure.channel", "Blueprint channel %s: %s",
            "Canal de blueprint %s: %s", "蓝图通道%s：%s");
    static final Tr MODES = Tr.of("typeomegalib.tooltip.modes", "Modes: %s", "Modos: %s", "模式：%s");

    public static final Tr POSITION = Tr.of("typeomegalib.tooltip.structure.position", "%s, %s, %s", "%s, %s, %s",
            "%s、%s、%s");
    public static final Tr FRONT = Tr.of("typeomegalib.tooltip.structure.front", "Front", "Frente", "正面");
    public static final Tr BACK = Tr.of("typeomegalib.tooltip.structure.back", "Back", "Fundo", "背面");
    public static final Tr DEPTH_N = Tr.of("typeomegalib.tooltip.structure.depth_n", "Layer %s from the front",
            "Camada %s a partir da frente", "自正面起第%s层");
    public static final Tr BOTTOM = Tr.of("typeomegalib.tooltip.structure.bottom", "bottom", "base", "底部");
    public static final Tr TOP = Tr.of("typeomegalib.tooltip.structure.top", "top", "topo", "顶部");
    public static final Tr MIDDLE = Tr.of("typeomegalib.tooltip.structure.middle", "middle", "meio", "中部");
    public static final Tr HEIGHT_N = Tr.of("typeomegalib.tooltip.structure.height_n", "layer %s from the bottom",
            "camada %s a partir da base", "自底部起第%s层");
    public static final Tr CENTER = Tr.of("typeomegalib.tooltip.structure.center", "center", "centro", "中央");
    public static final Tr COLUMN_N = Tr.of("typeomegalib.tooltip.structure.column_n", "column %s from the left",
            "coluna %s a partir da esquerda", "自左起第%s列");
    public static final Tr SPECIAL_BLOCK = Tr.of("typeomegalib.tooltip.structure.special_block", "Special block",
            "Bloco especial", "特殊方块");

    // EN: Names of the GTCEu part abilities, keyed by PartAbility.getName(); the size variants share one name.
    // PT: Nomes das PartAbility do GTCEu, pela chave de PartAbility.getName(); as variantes de tamanho dividem o nome.
    // ZH: GTCEu PartAbility 的名称，以 PartAbility.getName() 为键；不同尺寸的变体共用一个名称。
    private static final Map<String, Tr> PARTS = Map.ofEntries(
            part("export_items", "Output Bus", "Barramento de Saída", "输出总线"),
            part("import_items", "Input Bus", "Barramento de Entrada", "输入总线"),
            part("export_fluids", "Output Hatch", "Escotilha de Saída", "输出仓"),
            part("import_fluids", "Input Hatch", "Escotilha de Entrada", "输入仓"),
            part("input_energy", "Energy Hatch", "Escotilha de Energia", "能源仓"),
            part("output_energy", "Dynamo Hatch", "Escotilha de Dínamo", "动力仓"),
            part("substation_input_energy", "Substation Energy Hatch", "Escotilha de Energia de Subestação",
                    "变电站能源仓"),
            part("substation_output_energy", "Substation Dynamo Hatch", "Escotilha de Dínamo de Subestação",
                    "变电站动力仓"),
            part("rotor_holder", "Rotor Holder", "Suporte de Rotor", "转子支架"),
            part("pump_fluid_hatch", "Pump Fluid Hatch", "Escotilha de Fluido da Bomba", "泵流体仓"),
            part("steam", "Steam Hatch", "Escotilha de Vapor", "蒸汽仓"),
            part("steam_import_items", "Steam Input Bus", "Barramento de Entrada a Vapor", "蒸汽输入总线"),
            part("steam_export_items", "Steam Output Bus", "Barramento de Saída a Vapor", "蒸汽输出总线"),
            part("maintenance", "Maintenance Hatch", "Escotilha de Manutenção", "维护仓"),
            part("muffler", "Muffler Hatch", "Escotilha Silenciadora", "消声仓"),
            part("tank_valve", "Tank Valve", "Válvula de Tanque", "储罐阀门"),
            part("passthrough_hatch", "Passthrough Hatch", "Escotilha de Passagem", "直通仓"),
            part("parallel_hatch", "Parallel Control Hatch", "Escotilha de Controle Paralelo", "并行控制仓"),
            part("input_laser", "Laser Target Hatch", "Escotilha Alvo de Laser", "激光靶仓"),
            part("output_laser", "Laser Source Hatch", "Escotilha Fonte de Laser", "激光源仓"),
            part("computation_data_reception", "Computation Data Reception Hatch",
                    "Escotilha de Recepção de Computação", "算力接收仓"),
            part("computation_data_transmission", "Computation Data Transmission Hatch",
                    "Escotilha de Transmissão de Computação", "算力传输仓"),
            part("optical_data_reception", "Optical Data Reception Hatch", "Escotilha de Recepção de Dados Ópticos",
                    "光学数据接收仓"),
            part("optical_data_transmission", "Optical Data Transmission Hatch",
                    "Escotilha de Transmissão de Dados Ópticos", "光学数据传输仓"),
            part("data_access", "Data Access Hatch", "Escotilha de Acesso a Dados", "数据访问仓"),
            part("hpca_component", "HPCA Component", "Componente de HPCA", "HPCA组件"),
            part("object_holder", "Object Holder", "Suporte de Objeto", "物品支架"));

    private StructureTexts() {}

    private static Map.Entry<String, Tr> part(String ability, String en, String pt, String zh) {
        return Map.entry(ability, Tr.of("typeomegalib.tooltip.part." + ability, en, pt, zh));
    }

    /** The name of a GTCEu part ability, or {@code null} when the lib does not know it. */
    public static Tr partName(String ability) {
        String base = ability.replaceFirst("_(1|4|9)x$", "");
        return PARTS.get(base);
    }

    /** "A", "A or B", "A, B or C". */
    public static MutableComponent orList(List<? extends Component> items) {
        if (items.isEmpty()) return Component.empty();
        MutableComponent out = items.get(0).copy();
        for (int i = 1; i < items.size(); i++) {
            out = i == items.size() - 1 ? OR.component(out, items.get(i)) : out.append(", ").append(items.get(i));
        }
        return out;
    }

    static void init() {
        PARTS.size();
    }
}
