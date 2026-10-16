# Changelog

Todas as mudanças relevantes do Hora do Remédio.

## [1.2] - 2026-10-16

### Adicionado
- **Sinal vital de pressão e glicemia** no diário, com gráficos (linha dupla para pressão).
- **Backup completo** (medicamentos, doses, consultas, prontuário, diário, fotos) exportável/importável
  em JSON — salvável no Google Drive; nome do arquivo com data e registro do último backup.
- **Esquemas de uso**: diário, dias da semana ou "quando necessário" (SOS), com seção SOS na tela de Doses.
- **Controle de estoque** ativável por medicamento, com aviso de recompra e "medicamento esgotado".
- **Agrupamento de doses** do mesmo remédio no dia, com barra de progresso.
- **Foto por perfil** e exibição do perfil no "Geral"/diário.
- **Check-in diário** por notificação, respondível direto na barra.
- **Leitor de código de barras** (Google Code Scanner) no cadastro; busca por código.
- **Relatórios em PDF** com gráficos de adesão, sintomas e estoque.
- **Configurações em telas dedicadas** (Notificações e Configurações).
- **Idioma Espanhol** (além de Português e Inglês).
- **Layout responsivo**: NavigationRail em tablet/paisagem.

### Segurança
- **Banco criptografado** com SQLCipher (AES‑256), chave protegida pelo Android Keystore,
  com migração automática de bases antigas.
- Bloqueio por biometria (opcional).

### Build/Infra
- Release assinado (AAB/APK), regras de R8 para SQLCipher/ML Kit/Serialization.
- Release automático via GitHub Actions por tag.
- Testes: migrações (Room 3→11), criptografia, backup, geração de PDF e testes de UI (Compose).

## [1.1]
- Check-in, perfis com foto, estoque, tile/atalhos, lembretes de consulta/recompra.
