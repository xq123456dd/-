# 生成门锁模组的物品贴图（16x16 PNG）与 mod 图标（64x64 PNG）
# 运行方式： powershell -ExecutionPolicy Bypass -File tools\generate_textures.ps1
# 想改配色的话，直接改下面的 lockPalettes / keyPalettes 里的颜色值再跑一次即可。
Add-Type -AssemblyName System.Drawing
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$itemDir = Join-Path $root 'src\main\resources\assets\locksmith\textures\item'
New-Item -ItemType Directory -Force -Path $itemDir | Out-Null

# 挂锁图案：'.' 透明  'S' 锁梁  'L' 高光  'B' 主体  'D' 暗部  'K' 锁孔
$lockArt = @(
    '................',
    '....SSSSSSSS....',
    '...SS......SS...',
    '...SS......SS...',
    '...SS......SS...',
    '.LLLLLLLLLLLLLL.',
    '.LBBBBBBBBBBBBL.',
    '.LBBBBBKKBBBBBL.',
    '.LBBBBKKKKBBBBL.',
    '.LBBBBKKKKBBBBL.',
    '.LBBBBBKKBBBBBL.',
    '.LBBBBBKKBBBBBL.',
    '.LBBBBBBBBBBBBL.',
    '.DDDDDDDDDDDDDD.',
    '..DDDDDDDDDDDD..',
    '................'
)

# 钥匙图案
$keyArt = @(
    '................',
    '.....SSSSSS.....',
    '....SS....SS....',
    '....SS....SS....',
    '....SS....SS....',
    '.....SSSSSS.....',
    '.......BB.......',
    '.......BB.......',
    '.......BB.......',
    '.......BB.......',
    '.......BB.......',
    '.......BBBB.....',
    '.......BB.......',
    '.......BBBB.....',
    '.......BB.......',
    '................'
)

$lockPalettes = @{
    'wood_lock'      = @{ S = '#7a5230'; L = '#c08b52'; B = '#9a6b3c'; D = '#5f3d20'; K = '#20140a' }
    'stone_lock'     = @{ S = '#8a8a8a'; L = '#c0c0c0'; B = '#9b9b9b'; D = '#656565'; K = '#1d1d1d' }
    'iron_lock'      = @{ S = '#b9c0c6'; L = '#eef1f4'; B = '#ccd1d6'; D = '#8b9298'; K = '#1a1c1e' }
    'gold_lock'      = @{ S = '#e0b52c'; L = '#ffe884'; B = '#f0c542'; D = '#a97d15'; K = '#22190a' }
    'diamond_lock'   = @{ S = '#5fded4'; L = '#a6f5ee'; B = '#54d6cc'; D = '#2a9a91'; K = '#0d2624' }
    'netherite_lock' = @{ S = '#5c5257'; L = '#7d7176'; B = '#4a4247'; D = '#282124'; K = '#0f0c0d' }
}

$keyPalettes = @{
    'key'        = @{ S = '#d6dade'; B = '#b3b9be' }
    'master_key' = @{ S = '#c98cf0'; B = '#9d55d4' }
}

function Convert-HexToColor {
    param([string]$hex, [int]$alpha = 255)
    $r = [Convert]::ToInt32($hex.Substring(1, 2), 16)
    $g = [Convert]::ToInt32($hex.Substring(3, 2), 16)
    $b = [Convert]::ToInt32($hex.Substring(5, 2), 16)
    return [System.Drawing.Color]::FromArgb($alpha, $r, $g, $b)
}

function New-ArtBitmap {
    param([string[]]$art, [hashtable]$palette, [int]$scale = 1)
    $size = $art.Count
    $bmp = [System.Drawing.Bitmap]::new($size * $scale, $size * $scale, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    for ($y = 0; $y -lt $size; $y++) {
        $row = $art[$y]
        for ($x = 0; $x -lt $row.Length; $x++) {
            $ch = [string]$row[$x]
            if (-not $palette.ContainsKey($ch)) { continue }
            $color = Convert-HexToColor $palette[$ch]
            for ($dy = 0; $dy -lt $scale; $dy++) {
                for ($dx = 0; $dx -lt $scale; $dx++) {
                    $bmp.SetPixel($x * $scale + $dx, $y * $scale + $dy, $color)
                }
            }
        }
    }
    return $bmp
}

foreach ($name in $lockPalettes.Keys) {
    $bmp = New-ArtBitmap -art $lockArt -palette $lockPalettes[$name]
    $bmp.Save((Join-Path $itemDir "$name.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Output "生成 $name.png"
}

foreach ($name in $keyPalettes.Keys) {
    $bmp = New-ArtBitmap -art $keyArt -palette $keyPalettes[$name]
    $bmp.Save((Join-Path $itemDir "$name.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Output "生成 $name.png"
}

# mod 图标：把铁锁放大 4 倍（64x64）
$logo = New-ArtBitmap -art $lockArt -palette $lockPalettes['iron_lock'] -scale 4
$logo.Save((Join-Path $root 'src\main\resources\locksmith_logo.png'), [System.Drawing.Imaging.ImageFormat]::Png)
$logo.Dispose()
Write-Output '生成 locksmith_logo.png'
