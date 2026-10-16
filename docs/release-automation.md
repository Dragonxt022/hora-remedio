# Release automático (GitHub Actions)

O workflow [`.github/workflows/android-release.yml`](../.github/workflows/android-release.yml)
builda o **APK** e o **AAB** assinados e publica uma **Release** no GitHub.

## Como funciona

- Dispara ao **empurrar uma tag** `v*` (ex.: `v1.2`) ou manualmente (Actions ▸ Android Release ▸ Run workflow).
- Usa o **JDK 17** + Android SDK, gera `assembleRelease` e `bundleRelease`.
- Anexa `hora-do-remedio-<versao>.apk` e `hora-do-remedio-<versao>.aab` na release.

## Segredos necessários (Settings ▸ Secrets and variables ▸ Actions)

| Segredo | Descrição |
|---|---|
| `KEYSTORE_BASE64` | O arquivo `.jks` convertido para Base64 |
| `KEYSTORE_PASSWORD` | Senha do keystore (`storePassword`) |
| `KEY_ALIAS` | Alias da chave (ex.: `aprobile`) |
| `KEY_PASSWORD` | Senha da chave (`keyPassword`) |

### Gerar o `KEYSTORE_BASE64` (Windows/PowerShell)

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore\aprobile-release.jks"))
```

Copie toda a saída (uma linha longa) e cole no segredo `KEYSTORE_BASE64`.

### Gerar (macOS/Linux)

```bash
base64 -w0 keystore/aprobile-release.jks
```

## Publicar uma nova versão

1. Suba o `versionCode`/`versionName` em `app/build.gradle.kts`.
2. Commit e push para `main`.
3. Crie e empurre a tag:

```bash
git tag v1.3
git push origin v1.3
```

4. O GitHub Actions builda e cria a release automaticamente.

> Alternativa sem tag: dispare o workflow pelo botão **Run workflow** informando a tag.

## Observações

- O keystore **não** vai para o repositório (está no `.gitignore`); por isso ele é
  injetado via segredo e reconstruído no runner.
- `compileSdk`/`targetSdk` **37**: se o runner não tiver a plataforma disponível no canal
  estável, o workflow tenta o **canal preview** (`--channel=3`). Se ainda faltar, ajuste a
  versão do SDK no workflow.
- Para publicar na **Google Play**, use o `.aab` gerado (ou integre a Play Developer API).
