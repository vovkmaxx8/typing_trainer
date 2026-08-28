
#!/usr/bin/env python3
# typing_trainer.py
import argparse
import random
import time
import sys
import json
import csv
from colorama import init, Fore, Style

init(autoreset=True)

class TypingTrainer:
    def __init__(self, length=50, charset='all', mode='char', language='en'):
        self.length = length
        self.mode = mode
        self.language = language
        self.charset = self._get_charset(charset)
        self.text = self._generate_text()
        self.errors = 0
        self.total_chars = 0
        self.start_time = None
        self.end_time = None

    def _get_charset(self, charset):
        if self.language == 'ru':
            letters = 'абвгдеёжзийклмнопрстуфхцчшщъыьэюя'
            letters += letters.upper()
        else:
            letters = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'
        digits = '0123456789'
        symbols = '!@#$%^&*()_+-=,.;:?/[]{}|'
        if charset == 'letters':
            return letters
        elif charset == 'alnum':
            return letters + digits
        else:
            return letters + digits + symbols

    def _generate_text(self):
        if self.mode == 'word':
            words = []
            for _ in range(max(1, self.length // 5)):
                word_len = random.randint(3, 8)
                word = ''.join(random.choice(self.charset) for _ in range(word_len))
                words.append(word)
            return ' '.join(words)
        elif self.mode == 'sentence':
            sentences = []
            for _ in range(max(1, self.length // 30)):
                words_count = random.randint(5, 12)
                words = []
                for _ in range(words_count):
                    wl = random.randint(3, 8)
                    words.append(''.join(random.choice(self.charset) for _ in range(wl)))
                sentence = ' '.join(words).capitalize() + '.'
                sentences.append(sentence)
            return ' '.join(sentences)
        else:
            return ''.join(random.choice(self.charset) for _ in range(self.length))

    def run(self):
        print(Fore.CYAN + "Тренажер печати (символы)")
        print(Fore.YELLOW + "Введите следующий текст как можно точнее и быстрее:")
        print(Fore.WHITE + self.text)
        print("\nНажмите Enter, чтобы начать...")
        input()
        self.start_time = time.time()
        user_input = input(Fore.GREEN + "> ")
        self.end_time = time.time()
        self.total_chars = len(user_input)
        # Сравнение
        min_len = min(len(self.text), len(user_input))
        self.errors = sum(1 for i in range(min_len) if self.text[i] != user_input[i])
        if len(user_input) > len(self.text):
            self.errors += len(user_input) - len(self.text)
        # Вывод результатов
        elapsed = self.end_time - self.start_time
        cpm = (self.total_chars / elapsed) * 60 if elapsed > 0 else 0
        wpm = (self.total_chars / 5 / elapsed) * 60 if elapsed > 0 else 0
        accuracy = ((self.total_chars - self.errors) / self.total_chars * 100) if self.total_chars > 0 else 0
        print(Fore.CYAN + "\n--- Результаты ---")
        print(Fore.GREEN + f"Скорость: {cpm:.1f} симв/мин ({wpm:.1f} слов/мин)")
        print(Fore.YELLOW + f"Точность: {accuracy:.1f}%")
        print(Fore.MAGENTA + f"Время: {elapsed:.2f} сек")
        print(Fore.RED + f"Ошибок: {self.errors}")
        return {"cpm": cpm, "wpm": wpm, "accuracy": accuracy, "time": elapsed, "errors": self.errors}

def main():
    parser = argparse.ArgumentParser(description="Тренажер печати")
    parser.add_argument("--length", type=int, default=50, help="Длина текста")
    parser.add_argument("--charset", choices=["letters", "alnum", "all"], default="all")
    parser.add_argument("--mode", choices=["char", "word", "sentence"], default="char")
    parser.add_argument("--language", choices=["en", "ru"], default="en")
    parser.add_argument("--export-stats", help="Экспорт статистики в файл (JSON или CSV)")
    args = parser.parse_args()

    trainer = TypingTrainer(args.length, args.charset, args.mode, args.language)
    stats = trainer.run()
    if args.export_stats:
        ext = args.export_stats.split('.')[-1].lower()
        if ext == 'json':
            with open(args.export_stats, 'w') as f:
                json.dump(stats, f, indent=2)
        elif ext == 'csv':
            with open(args.export_stats, 'w', newline='') as f:
                writer = csv.DictWriter(f, fieldnames=stats.keys())
                writer.writeheader()
                writer.writerow(stats)
        print(f"Статистика сохранена в {args.export_stats}")

if __name__ == "__main__":
    main()
