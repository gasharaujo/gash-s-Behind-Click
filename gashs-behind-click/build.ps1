param(
    [switch]$Install
)

$ErrorActionPreference = 'Stop'

$projectRoot = [IO.Path]::GetFullPath($PSScriptRoot)
$devRoot = [IO.Path]::GetFullPath((Join-Path $projectRoot '..'))
$instanceRoot = [IO.Path]::GetFullPath((Join-Path $projectRoot '..\..'))
$minecraftRoot = [IO.Path]::GetFullPath((Join-Path $instanceRoot '..\..'))
$libraryRoot = Join-Path $minecraftRoot 'Install\libraries'
$buildDir = [IO.Path]::GetFullPath((Join-Path $projectRoot 'build'))
$classesDir = Join-Path $buildDir 'classes'
$libsDir = Join-Path $buildDir 'libs'
$outputJar = Join-Path $libsDir 'gashs-behind-click-1.0.0-neoforge-1.21.1.jar'

$devPrefix = $devRoot.TrimEnd([IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
$projectPrefix = $projectRoot.TrimEnd([IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
if (-not $projectRoot.StartsWith($devPrefix, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Projeto fora de DEV: $projectRoot"
}
if (-not $buildDir.StartsWith($projectPrefix, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Diretorio de build fora do projeto: $buildDir"
}

$jdk = Get-ChildItem 'C:\Program Files\Java' -Directory |
    Where-Object {
        (Test-Path (Join-Path $_.FullName 'bin\javac.exe')) -and
        (Test-Path (Join-Path $_.FullName 'bin\jar.exe')) -and
        (Test-Path (Join-Path $_.FullName 'bin\javap.exe'))
    } |
    Sort-Object Name -Descending |
    Select-Object -First 1

if (-not $jdk) {
    throw 'JDK com javac.exe, jar.exe e javap.exe nao encontrado.'
}

$javac = Join-Path $jdk.FullName 'bin\javac.exe'
$jarTool = Join-Path $jdk.FullName 'bin\jar.exe'
$javap = Join-Path $jdk.FullName 'bin\javap.exe'

$minecraftJar = Join-Path $libraryRoot 'net\minecraft\client\1.21.1-20240808.144430\client-1.21.1-20240808.144430-srg.jar'
$neoForgeClientJar = Join-Path $libraryRoot 'net\neoforged\neoforge\21.1.243\neoforge-21.1.243-client.jar'
$neoForgeJar = Join-Path $libraryRoot 'net\neoforged\neoforge\21.1.243\neoforge-21.1.243-universal.jar'
$fmlLoaderJar = Join-Path $libraryRoot 'net\neoforged\fancymodloader\loader\4.0.43\loader-4.0.43.jar'
$distMarkerJar = Join-Path $libraryRoot 'net\neoforged\mergetool\2.0.7\mergetool-2.0.7-api.jar'
$eventBusJar = Join-Path $libraryRoot 'net\neoforged\bus\8.0.5\bus-8.0.5.jar'
$jetbrainsAnnotationsJar = Join-Path $libraryRoot 'org\jetbrains\annotations\22.0.0\annotations-22.0.0.jar'

$dependencies = @(
    $neoForgeClientJar,
    $minecraftJar,
    $neoForgeJar,
    $fmlLoaderJar,
    $distMarkerJar,
    $eventBusJar,
    $jetbrainsAnnotationsJar
)

foreach ($dependency in $dependencies) {
    if (-not (Test-Path -LiteralPath $dependency)) {
        throw "Dependencia local nao encontrada: $dependency"
    }
}

if (Test-Path -LiteralPath $buildDir) {
    Remove-Item -LiteralPath $buildDir -Recurse -Force
}

New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path $libsDir -Force | Out-Null

$sources = Get-ChildItem (Join-Path $projectRoot 'src\main\java') -Recurse -Filter '*.java' |
    Select-Object -ExpandProperty FullName

if (-not $sources) {
    throw 'Nenhum fonte Java encontrado.'
}

$classpath = $dependencies -join [IO.Path]::PathSeparator
& $javac --release 21 -encoding UTF-8 -proc:none -classpath $classpath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) {
    throw "javac falhou com codigo $LASTEXITCODE"
}

Copy-Item (Join-Path $projectRoot 'src\main\resources\*') $classesDir -Recurse -Force

Push-Location $classesDir
try {
    & $jarTool --create --file $outputJar .
    if ($LASTEXITCODE -ne 0) {
        throw "jar falhou com codigo $LASTEXITCODE"
    }
} finally {
    Pop-Location
}

$requiredEntries = @(
    'META-INF/neoforge.mods.toml',
    'dev/gasharaujo/behindclick/GashsBehindClick.class',
    'assets/gashs_behind_click/lang/en_us.json',
    'assets/gashs_behind_click/lang/pt_br.json',
    'pack.mcmeta'
)
$jarEntries = @(& $jarTool tf $outputJar)
foreach ($entry in $requiredEntries) {
    if ($jarEntries -notcontains $entry) {
        throw "Entrada obrigatoria ausente do JAR: $entry"
    }
}

$classReport = @(& $javap -classpath $outputJar -verbose dev.gasharaujo.behindclick.GashsBehindClick)
if (-not ($classReport -match 'major version: 65')) {
    throw 'A classe compilada nao usa o formato esperado do Java 21.'
}

if ($Install) {
    $modsDir = [IO.Path]::GetFullPath((Join-Path $instanceRoot 'mods'))
    $expectedModsDir = [IO.Path]::GetFullPath((Join-Path $instanceRoot 'mods'))
    if (-not $modsDir.Equals($expectedModsDir, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Pasta mods inesperada: $modsDir"
    }

    $installedJar = [IO.Path]::GetFullPath((Join-Path $modsDir (Split-Path $outputJar -Leaf)))
    $modsPrefix = $modsDir.TrimEnd([IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    if (-not $installedJar.StartsWith($modsPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "JAR fora da pasta mods: $installedJar"
    }

    Copy-Item -LiteralPath $outputJar -Destination $installedJar -Force
}

Write-Output "JAR criado: $outputJar"
Write-Output 'Validacoes: conteudo do JAR e bytecode Java 21 aprovados.'
if ($Install) {
    Write-Output "JAR instalado: $(Join-Path $instanceRoot ('mods\' + (Split-Path $outputJar -Leaf)))"
}
