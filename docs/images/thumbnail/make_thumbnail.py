import json, sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter

REPO = r"C:\Users\wonjo\AndroidStudioProjects\CARSONA"
D = REPO + r"\app\src\main\res\drawable\\"
OUT = sys.argv[1]
W, H = 1280, 720
FB = r"C:\Windows\Fonts\malgunbd.ttf"
FR = r"C:\Windows\Fonts\malgun.ttf"

# 배경: 성장 공개 배경(빛줄기)을 16:9로 자르기
bg = Image.open(D + "bg_reveal.png").convert("RGB")
s = W / bg.width
bg = bg.resize((W, round(bg.height * s)), Image.LANCZOS)
top = 150
canvas = bg.crop((0, top, W, top + H)).convert("RGBA")

# 왼쪽 글씨가 잘 보이게 어두운 그라데이션
shade = Image.new("RGBA", (W, H), (0, 0, 0, 0))
sd = ImageDraw.Draw(shade)
for x in range(720):
    a = int(170 * (1 - x / 720) ** 1.4)
    sd.line([(x, 0), (x, H)], fill=(4, 12, 48, a))
canvas = Image.alpha_composite(canvas, shade)

place = json.load(open(REPO + r"\app\src\main\assets\avatar\placements.json", encoding="utf-8"))

def toto(stage, part, scale):
    c = Image.open(D + f"avatar_stage{stage}.png").convert("RGBA")
    if part:
        r = place[f"stage{stage}"][part]
        p = Image.open(D + f"part_{part}.png").convert("RGBA").resize((r["width"], r["height"]), Image.LANCZOS)
        c.alpha_composite(p, (r["x"], r["y"]))
    c = c.crop(c.getbbox())
    return c.resize((round(c.width * scale), round(c.height * scale)), Image.LANCZOS)

def shadow_paste(img, cx, base_y):
    x, y = cx - img.width // 2, base_y - img.height
    # 발밑 그림자
    sh = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(sh).ellipse([cx - img.width * 0.42, base_y - 14, cx + img.width * 0.42, base_y + 14], fill=(0, 0, 30, 110))
    sh = sh.filter(ImageFilter.GaussianBlur(8))
    canvas.alpha_composite(sh)
    canvas.alpha_composite(img, (x, y))

BASE = 680
sc = 0.5
for stage, part, cx in [(0, None, 735), (1, "leaf", 935), (2, "helmet", 1135)]:
    shadow_paste(toto(stage, part, sc), cx, BASE)

d = ImageDraw.Draw(canvas)
# 성장 화살표
for x0 in (823, 1025):
    d.polygon([(x0, 610), (x0 + 26, 626), (x0, 642)], fill=(255, 138, 61, 235))

def text(xy, s, font, fill, stroke=0, sfill=(6, 20, 63)):
    d.text(xy, s, font=font, fill=fill, stroke_width=stroke, stroke_fill=sfill)

text((64, 70), "카소랩 · Pleos Connect 차량용 앱", ImageFont.truetype(FR, 28), (169, 189, 230))
text((58, 112), "CARSONA", ImageFont.truetype(FB, 132), (246, 248, 255), 3)
text((66, 270), "카르소나", ImageFont.truetype(FB, 44), (168, 224, 122))

f = ImageFont.truetype(FB, 46)
text((64, 360), "차를 탈수록", f, (246, 248, 255), 2)
text((64, 420), "함께 자라는 아바타", f, (246, 248, 255), 2)
text((64, 480), "‘토토’", ImageFont.truetype(FB, 64), (255, 138, 61), 2)

# 아래 태그
tf = ImageFont.truetype(FB, 24)
tags = ["#안전운전", "#가족", "#성장"]
tx = 64
for t in tags:
    w = d.textlength(t, font=tf)
    d.rounded_rectangle([tx, 600, tx + w + 32, 644], radius=22, fill=(12, 33, 96, 230), outline=(36, 64, 127), width=2)
    d.text((tx + 16, 606), t, font=tf, fill=(246, 248, 255))
    tx += w + 46

canvas.convert("RGB").save(OUT, quality=95)
print("saved", OUT)
