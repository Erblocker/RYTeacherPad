package com.joanzapata.iconify.fonts;

import com.joanzapata.iconify.Icon;

public enum NovaIcons implements Icon {
    nova_icon_business_graph_line_2(''),
    nova_icon_business_graph_line_4(''),
    nova_icon_camera_2(''),
    nova_icon_check_bubble(''),
    nova_icon_check_circle_2(''),
    nova_icon_close(''),
    nova_icon_close_circle(''),
    nova_icon_content_filter(''),
    nova_icon_eco_recycle_bin(''),
    nova_icon_eraser(''),
    nova_icon_focus_2(''),
    nova_icon_mobile_phone_rotate_1(''),
    nova_icon_paint_brush_1(''),
    nova_icon_paint_palette(''),
    nova_icon_pencil_3(''),
    nova_icon_projector_screen(''),
    nova_icon_ruler_2(''),
    nova_icon_share_signal_tower(''),
    nova_icon_spelling_check_1(''),
    nova_icon_text_input_1(''),
    nova_icon_user_group_view(''),
    nova_icon_window_view_1('');
    
    char character;

    private NovaIcons(char character) {
        this.character = character;
    }

    public String key() {
        return name().replace('_', '-');
    }

    public char character() {
        return this.character;
    }
}
