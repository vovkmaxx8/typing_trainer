# Тренажер печати (символы)

Многоязычное консольное приложение для тренировки слепой печати на клавиатуре.  
Генерирует случайные последовательности символов (буквы, цифры, знаки препинания) и измеряет скорость и точность ввода.

## Особенности
- Генерация случайных текстов из букв (латиница/кириллица), цифр и специальных символов.
- Настройка длины текста и набора символов.
- Режимы: посимвольный, пословный, по предложениям.
- Подсчёт скорости печати (символов/минуту, слов/минуту) и точности (в процентах).
- Отображение ошибок в реальном времени (цветовая индикация).
- Статистика за сессию: средняя скорость, точность, время.
- Экспорт результатов в JSON/CSV.
- Цветной вывод в терминале (где поддерживается).
- Поддержка аргументов командной строки для быстрого запуска.

## Установка и запуск
Для каждого языка требуются соответствующие инструменты.

### Запуск на разных языках

1. **Python**  
   Установка: `pip install colorama`  
   Запуск: `python typing_trainer.py --length 50 --charset all`

2. **JavaScript (Node.js)**  
   Установка: `npm install commander chalk`  
   Запуск: `node typing_trainer.js --length 50 --charset all`

3. **Go**  
   Запуск: `go run typing_trainer.go --length 50 --charset all`

4. **Rust**  
   Сборка: `cargo build --release`  
   Запуск: `cargo run -- --length 50 --charset all`

5. **Java**  
   Сборка: `javac -cp gson.jar TypingTrainer.java`  
   Запуск: `java -cp .;gson.jar TypingTrainer --length 50`

6. **C# (.NET Core)**  
   Запуск: `dotnet run -- --length 50 --charset all`

7. **C++ (Linux)**  
   Сборка: `g++ -std=c++11 -o typing_trainer typing_trainer.cpp`  
   Запуск: `./typing_trainer --length 50`

8. **Kotlin (JVM)**  
   Сборка: `kotlinc -cp gson.jar TypingTrainer.kt`  
   Запуск: `kotlin -cp .;gson.jar TypingTrainerKt --length 50`

## Использование

Общие аргументы командной строки:

- `--length <число>` – количество символов в тексте (по умолчанию 50).
- `--charset <набор>` – `letters` (только буквы), `alnum` (буквы+цифры), `all` (все символы). По умолчанию `all`.
- `--mode <режим>` – `char` (посимвольно), `word` (пословно), `sentence` (предложения). По умолчанию `char`.
- `--language <язык>` – `en` или `ru` (по умолчанию `en`).
- `--export-stats <файл>` – экспортировать статистику (JSON/CSV).
- `--help` – справка.

Пример (Python):
```bash
python typing_trainer.py --length 100 --charset alnum --mode word --language ru
Структура репозитория
text
/
├── README.md
├── typing_trainer.py
├── typing_trainer.js
├── typing_trainer.go
├── typing_trainer.rs
├── TypingTrainer.java
├── TypingTrainer.cs
├── typing_trainer.cpp
└── TypingTrainer.kt
Лицензия
MIT
