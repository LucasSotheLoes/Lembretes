# Lembretes — agenda com notificações (Android)

App simples para não esquecer trabalhos, provas, cobranças e eventos.
Kotlin + Jetpack Compose (Material 3), sem servidor e sem conta: tudo fica no celular.

## Como funciona
- Cada evento tem título, tipo (Trabalho, Prova, Cobrança, Evento, Outro), data, hora, observações
  e um ou mais avisos (na hora, 15 min, 1 h, 3 h, 1 dia, 2 dias ou 1 semana antes).
- Os avisos são agendados com `AlarmManager` (alarmes exatos) e disparam uma notificação
  mesmo com o app fechado.
- Ao reiniciar o celular, os alarmes são recriados (`BootReceiver`).
- Os dados ficam em `eventos.json` no armazenamento interno do app.

## Rodar no celular (Android Studio)
1. Abra a pasta `Lembretes` no Android Studio (File > Open) e espere o Gradle sincronizar.
2. No celular: Configurações > Sobre o telefone > toque 7× em "Número da versão" para liberar
   as Opções do desenvolvedor; depois ative "Depuração USB".
3. Ligue o celular no USB, escolha ele na barra de cima e clique em Run (▶).

Para gerar um APK e instalar sem cabo: Build > Build App Bundle(s) / APK(s) > Build APK(s).
O arquivo sai em `app/build/outputs/apk/debug/app-debug.apk`.

## Gerar o APK pelo GitHub (sem Android Studio)
O projeto tem `.github/workflows/build.yml`. Suba a pasta para um repositório no GitHub;
a cada push a aba Actions compila e deixa o APK para download em "Artifacts".

## Importante: economia de bateria
Xiaomi, Samsung, Motorola etc. às vezes matam alarmes de apps "não otimizados".
Se um aviso não chegar: Configurações > Apps > Lembretes > Bateria > **Sem restrições**.

## Estrutura
```
app/src/main/java/br/com/lucas/lembretes/
  Event.kt              modelo, categorias e opções de aviso
  EventRepository.kt    leitura/gravação do JSON + StateFlow para a UI
  ReminderScheduler.kt  agenda/cancela alarmes
  Notifications.kt      canal e montagem da notificação
  Receivers.kt          ReminderReceiver (dispara aviso) e BootReceiver
  Format.kt             datas em pt-BR ("amanhã às 14:00", "em 3 dias")
  MainActivity.kt       permissões e navegação lista <-> edição
  ui/ListScreen.kt      lista agrupada por dia
  ui/EditScreen.kt      criar/editar evento
```
