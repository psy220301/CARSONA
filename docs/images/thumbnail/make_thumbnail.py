"""CARSONA 시연 영상 썸네일(1280x720) 만들기.

사용법: python docs/images/thumbnail/make_thumbnail.py [출력 경로]
- 앱에 들어간 이미지(배경, 토토 3단계, 꾸미기 파츠)만 사용한다.
- 글꼴: Noto Sans KR (SIL Open Font License). 경로는 환경 변수 NOTO_SANS_KR로 바꿀 수 있다.
"""
import json
import os
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", "..", ".."))
DRAWABLE = os.path.join(REPO, "app", "src", "main", "res", "drawable")
PLACEMENTS = os.path.join(REPO, "app", "src", "main", "assets", "avatar", "placements.json")
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "CARSONA_thumbnail_1280x720.png")
NOTO = os.environ.get("NOTO_SANS_KR", r"C:\Windows\Fonts\NotoSansKR-VF.ttf")
W, H = 1280, 720


def font(size, weight="Bold"):
    f = ImageFont.truetype(NOTO, size)
    try:
        f.set_variation_by_name(weight)  # 가변 글꼴 굵기
    except Exception:
        pass
    return f


def drawable(name):
    return Image.open(os.path.join(DRAWABLE, name + ".png"))


# 배경: 성장 공개 배경(빛줄기)을 16:9로 자르기
bg = drawable("bg_reveal").convert("RGB")
bg = bg.resize((W, round(bg.height * W / bg.width)), Image.LANCZOS)
canvas = bg.crop((0, 150, W, 150 + H)).convert("RGBA")

# 왼쪽 글씨가 잘 보이게 어두운 그라데이션
shade = Image.new("RGBA", (W, H), (0, 0, 0, 0))
sd = ImageDraw.Draw(shade)
for x in range(720):
    sd.line([(x, 0), (x, H)], fill=(4, 12, 48, int(170 * (1 - x / 720) ** 1.4)))
canvas = Image.alpha_composite(canvas, shade)

with open(PLACEMENTS, encoding="utf-8") as fp:
    place = json.load(fp)


def toto(stage, part, scale):
    c = drawable(f"avatar_stage{stage}").convert("RGBA")
    if part:
        r = place[f"stage{stage}"][part]
        p = drawable(f"part_{part}").convert("RGBA").resize((r["width"], r["height"]), Image.LANCZOS)
        c.alpha_composite(p, (r["x"], r["y"]))
    c = c.crop(c.getbbox())
    return c.resize((round(c.width * scale), round(c.height * scale)), Image.LANCZOS)


def paste_with_shadow(img, cx, base_y):
    sh = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(sh).ellipse(
        [cx - img.width * 0.42, base_y - 14, cx + img.width * 0.42, base_y + 14], fill=(0, 0, 30, 110))
    canvas.alpha_composite(sh.filter(ImageFilter.GaussianBlur(8)))
    canvas.alpha_composite(img, (cx - img.width // 2, base_y - img.height))


# 아기(맨머리) → 청소년(새싹) → 성인(헬멧)
for stage, part, cx in [(0, None, 735), (1, "leaf", 935), (2, "helmet", 1135)]:
    paste_with_shadow(toto(stage, part, 0.5), cx, 680)

d = ImageDraw.Draw(canvas)
for x0 in (823, 1025):  # 성장 화살표
    d.polygon([(x0, 610), (x0 + 26, 626), (x0, 642)], fill=(255, 138, 61, 235))


def text(xy, s, f, fill, stroke=0):
    d.text(xy, s, font=f, fill=fill, stroke_width=stroke, stroke_fill=(6, 20, 63))


text((64, 62), "카소랩 · Pleos Connect 차량용 앱", font(28, "Regular"), (169, 189, 230))
text((58, 92), "CARSONA", font(132, "Black"), (246, 248, 255), 3)
text((66, 262), "카르소나", font(44), (168, 224, 122))
text((64, 352), "차를 탈수록", font(46), (246, 248, 255), 2)
text((64, 412), "함께 자라는 아바타", font(46), (246, 248, 255), 2)
text((64, 468), "‘토토’", font(64, "Black"), (255, 138, 61), 2)

tf = font(24)
tx = 64
for t in ["#안전운전", "#가족", "#성장"]:
    w = d.textlength(t, font=tf)
    d.rounded_rectangle([tx, 600, tx + w + 32, 644], radius=22, fill=(12, 33, 96, 230), outline=(36, 64, 127), width=2)
    d.text((tx + 16, 603), t, font=tf, fill=(246, 248, 255))
    tx += w + 46

canvas.convert("RGB").save(OUT)
print("saved", OUT)
