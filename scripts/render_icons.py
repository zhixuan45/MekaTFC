import json
import math
import os
import zipfile
import numpy as np
from PIL import Image

def get_tfc_jar():
    return r'C:\Users\JuziD\.gradle\caches\modules-2\files-2.1\maven.modrinth\terrafirmacraft\3.2.24\e1b65ace1199044a982377d27438c63a4418a744\terrafirmacraft-3.2.24.jar'

def get_mek_jar():
    return r'C:\Users\JuziD\.gradle\caches\forge_gradle\deobf_dependencies\mekanism\Mekanism\1.20.1-10.4.16.80_mapped_parchment_2023.09.03-1.20.1\Mekanism-1.20.1-10.4.16.80_mapped_parchment_2023.09.03-1.20.1.jar'

def load_texture_from_jar(jar_path, texture_entry):
    if not texture_entry.endswith('.png'):
        texture_entry += '.png'
    with zipfile.ZipFile(jar_path, 'r') as z:
        with z.open(texture_entry) as f:
            return Image.open(f).convert('RGBA')

def load_texture(texture_id, project_root):
    if texture_id.startswith('tfc:'):
        rel = 'assets/tfc/textures/' + texture_id[4:] + '.png'
        return load_texture_from_jar(get_tfc_jar(), rel)
    elif texture_id.startswith('mekanism:'):
        rel = 'assets/mekanism/textures/' + texture_id[9:] + '.png'
        return load_texture_from_jar(get_mek_jar(), rel)
    elif texture_id.startswith('mekatfc:'):
        rel = os.path.join(project_root, 'src', 'main', 'resources', 'assets', 'mekatfc', 'textures', texture_id[8:] + '.png')
        return Image.open(rel).convert('RGBA')
    raise ValueError(f"Unknown texture source: {texture_id}")

class ModelRenderer:
    def __init__(self, size=512, supersample=4):
        self.output_size = size
        self.ss = supersample
        self.render_size = size * supersample

    def render_model(self, elements, texture_map, display_cfg=None, custom_rot=None, custom_trans=None, custom_scale=None):
        width = height = self.render_size
        z_buffer = np.full((height, width), -1e9, dtype=np.float32)
        frame_buffer = np.zeros((height, width, 4), dtype=np.uint8)

        # Standard Minecraft GUI transform:
        # Looking down at 30 degrees pitch, 45 degrees yaw (showing Up, West/North, South/East)
        # Minecraft json rotation: [30, 225, 0] or [30, 45, 0]
        # In Minecraft matrix stack:
        # pitch 30 deg (X rotation), yaw 45 deg (Y rotation)
        rot = [30, 45, 0]
        trans = [0.0, 0.0, 0.0]
        scale = [1.0, 1.0, 1.0]

        if display_cfg and 'gui' in display_cfg:
            gui = display_cfg['gui']
            if 'rotation' in gui:
                # If rotation is [30, 225, 0], in standard camera space it is pitch 30, yaw 45
                r = gui['rotation']
                rot = [r[0], r[1], r[2]]
            if 'translation' in gui:
                trans = gui['translation']
            if 'scale' in gui:
                scale = gui['scale']

        if custom_rot is not None:
            rot = custom_rot
        if custom_trans is not None:
            trans = custom_trans
        if custom_scale is not None:
            scale = custom_scale

        # Minecraft ItemRenderer GUI transformation:
        # PoseStack: translate(8, 8, 8) -> rotate(rot) -> translate(-8, -8, -8) + trans
        # Standard GUI rotation is: rotX = 30 deg, rotY = 45 (or 225) deg
        rx = math.radians(rot[0])
        ry = math.radians(rot[1])
        rz = math.radians(rot[2])

        cos_x, sin_x = math.cos(rx), math.sin(rx)
        cos_y, sin_y = math.cos(ry), math.sin(ry)
        cos_z, sin_z = math.cos(rz), math.sin(rz)

        def transform_point(p):
            # p in [0, 16]
            x = (p[0] - 8.0) * scale[0] + trans[0]
            y = (p[1] - 8.0) * scale[1] + trans[1]
            z = (p[2] - 8.0) * scale[2] + trans[2]

            # Rotate Y (yaw)
            x1 = x * cos_y + z * sin_y
            y1 = y
            z1 = -x * sin_y + z * cos_y

            # Rotate X (pitch down) -> +Y rotates towards -Z, +Z rotates towards +Y
            # To look down from above: Y_cam = y1 * cos(30) - z1 * sin(30), Z_cam = y1 * sin(30) + z1 * cos(30)
            x2 = x1
            y2 = y1 * cos_x - z1 * sin_x
            z2 = y1 * sin_x + z1 * cos_x

            # Rotate Z (roll)
            x3 = x2 * cos_z - y2 * sin_z
            y3 = x2 * sin_z + y2 * cos_z
            z3 = z2

            # Screen projection: orthographic
            # Minecraft GUI: 16 units is 1 block width in slot
            base_scale = (self.render_size * 0.72) / (16.0 * math.sqrt(3))
            screen_x = width / 2.0 + x3 * base_scale
            screen_y = height / 2.0 - y3 * base_scale  # +Y is UP in world -> -Y on screen
            screen_z = z3 # larger Z is closer to camera

            return np.array([screen_x, screen_y, screen_z], dtype=np.float32)

        polygons = []
        for elem in elements:
            f = elem['from']
            t = elem['to']

            elem_faces = elem.get('faces', {})
            face_defs = {
                'north': {
                    'quad': [[t[0], t[1], f[2]], [f[0], t[1], f[2]], [f[0], f[1], f[2]], [t[0], f[1], f[2]]],
                    'default_uv': [f[0], 16 - t[1], t[0], 16 - f[1]],
                    'shade': 0.8
                },
                'south': {
                    'quad': [[f[0], t[1], t[2]], [t[0], t[1], t[2]], [t[0], f[1], t[2]], [f[0], f[1], t[2]]],
                    'default_uv': [f[0], 16 - t[1], t[0], 16 - f[1]],
                    'shade': 0.6
                },
                'west': {
                    'quad': [[f[0], t[1], f[2]], [f[0], t[1], t[2]], [f[0], f[1], t[2]], [f[0], f[1], f[2]]],
                    'default_uv': [f[2], 16 - t[1], t[2], 16 - f[1]],
                    'shade': 0.8
                },
                'east': {
                    'quad': [[t[0], t[1], t[2]], [t[0], t[1], f[2]], [t[0], f[1], f[2]], [t[0], f[1], t[2]]],
                    'default_uv': [16 - t[2], 16 - t[1], 16 - f[2], 16 - f[1]],
                    'shade': 0.6
                },
                'up': {
                    'quad': [[f[0], t[1], f[2]], [t[0], t[1], f[2]], [t[0], t[1], t[2]], [f[0], t[1], t[2]]],
                    'default_uv': [f[0], f[2], t[0], t[2]],
                    'shade': 1.0
                },
                'down': {
                    'quad': [[f[0], f[1], t[2]], [t[0], f[1], t[2]], [t[0], f[1], f[2]], [f[0], f[1], f[2]]],
                    'default_uv': [f[0], 16 - t[2], t[0], 16 - f[2]],
                    'shade': 0.5
                }
            }

            for side, face_data in elem_faces.items():
                if side not in face_defs:
                    continue
                tex_ref = face_data.get('texture', '')
                if tex_ref.startswith('#'):
                    tex_name = texture_map.get(tex_ref[1:], None)
                else:
                    tex_name = texture_map.get(tex_ref, None)
                
                if not tex_name:
                    continue

                quad_3d = face_defs[side]['quad']
                uv = face_data.get('uv', face_defs[side]['default_uv'])
                uv_rot = face_data.get('rotation', 0)
                shade = face_defs[side]['shade']

                screen_quad = [transform_point(pt) for pt in quad_3d]

                # Backface culling in screen space
                v0, v1, v2 = screen_quad[0], screen_quad[1], screen_quad[2]
                area = (v1[0] - v0[0]) * (v2[1] - v0[1]) - (v1[1] - v0[1]) * (v2[0] - v0[0])
                if area > 0: # counter-clockwise on screen where +Y is down
                    polygons.append({
                        'quad': screen_quad,
                        'uv': uv,
                        'uv_rot': uv_rot,
                        'tex': tex_name,
                        'shade': shade
                    })

        self.rasterize(polygons, frame_buffer, z_buffer)

        img = Image.fromarray(frame_buffer, 'RGBA')
        # Crop to tight bounding box with comfortable padding
        bbox = img.getbbox()
        if bbox:
            # Add 24px padding relative to render size
            pad = int(self.render_size * 0.05)
            crop_box = (
                max(0, bbox[0] - pad),
                max(0, bbox[1] - pad),
                min(self.render_size, bbox[2] + pad),
                min(self.render_size, bbox[3] + pad)
            )
            # Make square
            w = crop_box[2] - crop_box[0]
            h = crop_box[3] - crop_box[1]
            max_dim = max(w, h)
            cx = (crop_box[0] + crop_box[2]) / 2.0
            cy = (crop_box[1] + crop_box[3]) / 2.0
            square_box = (
                max(0, int(cx - max_dim / 2.0)),
                max(0, int(cy - max_dim / 2.0)),
                min(self.render_size, int(cx + max_dim / 2.0)),
                min(self.render_size, int(cy + max_dim / 2.0))
            )
            img = img.crop(square_box)

        img = img.resize((self.output_size, self.output_size), Image.Resampling.LANCZOS)
        return img

    def rasterize(self, polygons, frame_buffer, z_buffer):
        for poly in polygons:
            quad = poly['quad']
            uv = poly['uv']
            tex = poly['tex']
            shade = poly['shade']
            uv_rot = poly['uv_rot']

            tex_w, tex_h = tex.size
            tex_np = np.array(tex)

            u0, v0, u1, v1 = uv[0] / 16.0, uv[1] / 16.0, uv[2] / 16.0, uv[3] / 16.0
            uv_corners = np.array([
                [u0, v0],
                [u1, v0],
                [u1, v1],
                [u0, v1]
            ], dtype=np.float32)

            if uv_rot == 90:
                uv_corners = np.roll(uv_corners, -1, axis=0)
            elif uv_rot == 180:
                uv_corners = np.roll(uv_corners, -2, axis=0)
            elif uv_rot == 270:
                uv_corners = np.roll(uv_corners, -3, axis=0)

            tris = [
                ((quad[0], quad[1], quad[2]), (uv_corners[0], uv_corners[1], uv_corners[2])),
                ((quad[0], quad[2], quad[3]), (uv_corners[0], uv_corners[2], uv_corners[3]))
            ]

            for tri_pts, tri_uvs in tris:
                self.draw_triangle(tri_pts, tri_uvs, tex_np, shade, frame_buffer, z_buffer)

    def draw_triangle(self, pts, uvs, tex_np, shade, fb, zb):
        p0, p1, p2 = pts
        uv0, uv1, uv2 = uvs

        min_x = max(0, int(min(p0[0], p1[0], p2[0])))
        max_x = min(self.render_size - 1, int(max(p0[0], p1[0], p2[0])) + 1)
        min_y = max(0, int(min(p0[1], p1[1], p2[1])))
        max_y = min(self.render_size - 1, int(max(p0[1], p1[1], p2[1])) + 1)

        if min_x > max_x or min_y > max_y:
            return

        tex_h, tex_w, _ = tex_np.shape

        denom = (p1[1] - p2[1]) * (p0[0] - p2[0]) + (p2[0] - p1[0]) * (p0[1] - p2[1])
        if abs(denom) < 1e-6:
            return

        inv_denom = 1.0 / denom

        xs = np.arange(min_x, max_x + 1) + 0.5
        ys = np.arange(min_y, max_y + 1) + 0.5
        grid_x, grid_y = np.meshgrid(xs, ys)

        w0 = ((p1[1] - p2[1]) * (grid_x - p2[0]) + (p2[0] - p1[0]) * (grid_y - p2[1])) * inv_denom
        w1 = ((p2[1] - p0[1]) * (grid_x - p2[0]) + (p0[0] - p2[0]) * (grid_y - p2[1])) * inv_denom
        w2 = 1.0 - w0 - w1

        mask = (w0 >= 0) & (w1 >= 0) & (w2 >= 0)
        if not np.any(mask):
            return

        interp_z = w0 * p0[2] + w1 * p1[2] + w2 * p2[2]

        interp_u = w0 * uv0[0] + w1 * uv1[0] + w2 * uv2[0]
        interp_v = w0 * uv0[1] + w1 * uv1[1] + w2 * uv2[1]

        tex_x = np.clip((interp_u * tex_w).astype(np.int32), 0, tex_w - 1)
        tex_y = np.clip((interp_v * tex_h).astype(np.int32), 0, tex_h - 1)

        colors = tex_np[tex_y, tex_x]
        shaded_rgb = np.clip(colors[:, :, :3].astype(np.float32) * shade, 0, 255).astype(np.uint8)
        alpha = colors[:, :, 3]

        y_indices, x_indices = np.where(mask & (alpha > 10))
        for y_idx, x_idx in zip(y_indices, x_indices):
            py = min_y + y_idx
            px = min_x + x_idx
            pz = interp_z[y_idx, x_idx]

            if pz >= zb[py, px] - 0.005:
                cur_a = alpha[y_idx, x_idx]
                if cur_a == 255:
                    zb[py, px] = pz
                    fb[py, px, :3] = shaded_rgb[y_idx, x_idx]
                    fb[py, px, 3] = 255
                else:
                    src_rgb = shaded_rgb[y_idx, x_idx].astype(np.float32)
                    src_a = cur_a / 255.0
                    dst_rgb = fb[py, px, :3].astype(np.float32)
                    dst_a = fb[py, px, 3] / 255.0
                    out_a = src_a + dst_a * (1.0 - src_a)
                    if out_a > 0:
                        out_rgb = (src_rgb * src_a + dst_rgb * dst_a * (1.0 - src_a)) / out_a
                        fb[py, px, :3] = np.clip(out_rgb, 0, 255).astype(np.uint8)
                        fb[py, px, 3] = int(out_a * 255)
                        zb[py, px] = pz

def main():
    project_root = r'c:\Users\JuziD\IdeaProjects\MekaTFC'
    out_dir = os.path.join(project_root, 'exported_icons')
    os.makedirs(out_dir, exist_ok=True)

    renderer = ModelRenderer(size=512, supersample=4)

    # 1. TFC Steel Anvil
    print("Rendering TFC Steel Anvil...")
    tfc_jar = get_tfc_jar()
    with zipfile.ZipFile(tfc_jar, 'r') as z:
        anvil_model = json.loads(z.read('assets/tfc/models/block/anvil.json').decode('utf-8'))
    
    steel_tex = load_texture('tfc:block/metal/smooth/steel', project_root)
    anvil_tex_map = {'all': steel_tex}
    
    # Anvil in GUI: rot [30, 45, 0], translation [0, 1, 0]
    img_anvil = renderer.render_model(
        anvil_model['elements'],
        anvil_tex_map,
        custom_rot=[30, 45, 0],
        custom_trans=[0, 0, 0],
        custom_scale=[1.0, 1.0, 1.0]
    )
    anvil_path = os.path.join(out_dir, 'tfc_steel_anvil.png')
    img_anvil.save(anvil_path)
    print(f"Saved: {anvil_path}")

    # 2. Mekanism Metallurgic Infuser
    print("Rendering Mekanism Metallurgic Infuser...")
    mek_jar = get_mek_jar()
    with zipfile.ZipFile(mek_jar, 'r') as z:
        infuser_model = json.loads(z.read('assets/mekanism/models/block/metallurgic_infuser.json').decode('utf-8'))
    
    infuser_all_tex = load_texture('mekanism:block/models/metallurgic_infuser', project_root)
    infuser_elements_tex = load_texture('mekanism:block/models/metallurgic_infuser_elements', project_root)
    infuser_tex_map = {
        'all': infuser_all_tex,
        '2': infuser_elements_tex
    }
    img_infuser = renderer.render_model(
        infuser_model['elements'],
        infuser_tex_map,
        custom_rot=[30, 45, 0],
        custom_scale=[1.0, 1.0, 1.0]
    )
    infuser_path = os.path.join(out_dir, 'mekanism_metallurgic_infuser.png')
    img_infuser.save(infuser_path)
    print(f"Saved: {infuser_path}")

    # 3. MekaTFC Native Osmium Ore (Granite base + Normal Native Osmium overlay)
    print("Rendering MekaTFC Native Osmium Ore...")
    granite_tex = load_texture('tfc:block/rock/raw/granite', project_root)
    osmium_overlay_tex = load_texture('mekatfc:block/ore/normal_native_osmium', project_root)
    
    with zipfile.ZipFile(tfc_jar, 'r') as z:
        ore_model = json.loads(z.read('assets/tfc/models/block/ore.json').decode('utf-8'))
    
    ore_tex_map = {
        'all': granite_tex,
        'overlay': osmium_overlay_tex
    }
    img_ore = renderer.render_model(
        ore_model['elements'],
        ore_tex_map,
        custom_rot=[30, 45, 0],
        custom_scale=[1.0, 1.0, 1.0]
    )
    ore_path = os.path.join(out_dir, 'mekatfc_native_osmium_ore.png')
    img_ore.save(ore_path)
    print(f"Saved: {ore_path}")

    # Also render poor and rich grades
    for grade in ['poor', 'rich']:
        overlay_tex = load_texture(f'mekatfc:block/ore/{grade}_native_osmium', project_root)
        grade_tex_map = {'all': granite_tex, 'overlay': overlay_tex}
        img_grade_ore = renderer.render_model(
            ore_model['elements'],
            grade_tex_map,
            custom_rot=[30, 45, 0],
            custom_scale=[1.0, 1.0, 1.0]
        )
        p = os.path.join(out_dir, f'mekatfc_{grade}_native_osmium_ore.png')
        img_grade_ore.save(p)
        print(f"Saved: {p}")

if __name__ == '__main__':
    main()
