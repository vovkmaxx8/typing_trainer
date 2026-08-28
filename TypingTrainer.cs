// TypingTrainer.cs
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace TypingTrainer
{
    class Program
    {
        static void Main(string[] args)
        {
            var opts = ParseArgs(args);
            var trainer = new TypingTrainer(opts.Length, opts.Charset, opts.Mode, opts.Language);
            var stats = trainer.Run();
            if (opts.ExportStats != null)
            {
                string json = JsonSerializer.Serialize(stats, new JsonSerializerOptions { WriteIndented = true });
                File.WriteAllText(opts.ExportStats, json);
                Console.WriteLine($"Статистика сохранена в {opts.ExportStats}");
            }
        }

        static Options ParseArgs(string[] args)
        {
            var opts = new Options();
            for (int i = 0; i < args.Length; i++)
            {
                switch (args[i])
                {
                    case "--length": opts.Length = int.Parse(args[++i]); break;
                    case "--charset": opts.Charset = args[++i]; break;
                    case "--mode": opts.Mode = args[++i]; break;
                    case "--language": opts.Language = args[++i]; break;
                    case "--export-stats": opts.ExportStats = args[++i]; break;
                }
            }
            return opts;
        }

        class Options
        {
            public int Length { get; set; } = 50;
            public string Charset { get; set; } = "all";
            public string Mode { get; set; } = "char";
            public string Language { get; set; } = "en";
            public string ExportStats { get; set; }
        }

        class TypingTrainer
        {
            private readonly int length;
            private readonly string charset;
            private readonly string mode;
            private readonly string language;
            private readonly string text;
            private int errors;
            private int totalChars;
            private DateTime start;
            private DateTime end;

            public TypingTrainer(int len, string cs, string m, string lang)
            {
                length = len;
                charset = cs;
                mode = m;
                language = lang;
                text = GenerateText();
            }

            private string GetCharset()
            {
                string letters = language == "ru" ?
                    "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" :
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                string digits = "0123456789";
                string symbols = "!@#$%^&*()_+-=,.;:?/[]{}|";
                return charset switch
                {
                    "letters" => letters,
                    "alnum" => letters + digits,
                    _ => letters + digits + symbols,
                };
            }

            private string GenerateText()
            {
                var cs = GetCharset();
                var rand = new Random();
                if (mode == "word")
                {
                    int wordCount = Math.Max(1, length / 5);
                    var words = new List<string>();
                    for (int i = 0; i < wordCount; i++)
                    {
                        int wl = rand.Next(3, 8);
                        char[] w = new char[wl];
                        for (int j = 0; j < wl; j++) w[j] = cs[rand.Next(cs.Length)];
                        words.Add(new string(w));
                    }
                    return string.Join(" ", words);
                }
                else if (mode == "sentence")
                {
                    int sentCount = Math.Max(1, length / 30);
                    var sentences = new List<string>();
                    for (int i = 0; i < sentCount; i++)
                    {
                        int wc = rand.Next(5, 12);
                        var words = new List<string>();
                        for (int j = 0; j < wc; j++)
                        {
                            int wl = rand.Next(3, 8);
                            char[] w = new char[wl];
                            for (int k = 0; k < wl; k++) w[k] = cs[rand.Next(cs.Length)];
                            words.Add(new string(w));
                        }
                        string sent = string.Join(" ", words);
                        sent = char.ToUpper(sent[0]) + sent.Substring(1) + ".";
                        sentences.Add(sent);
                    }
                    return string.Join(" ", sentences);
                }
                else
                {
                    char[] chars = new char[length];
                    for (int i = 0; i < length; i++) chars[i] = cs[rand.Next(cs.Length)];
                    return new string(chars);
                }
            }

            public Dictionary<string, object> Run()
            {
                Console.ForegroundColor = ConsoleColor.Cyan;
                Console.WriteLine("Тренажер печати (символы)");
                Console.ForegroundColor = ConsoleColor.Yellow;
                Console.WriteLine("Введите следующий текст как можно точнее и быстрее:");
                Console.ForegroundColor = ConsoleColor.White;
                Console.WriteLine(text);
                Console.ResetColor();
                Console.WriteLine("\nНажмите Enter, чтобы начать...");
                Console.ReadLine();
                Console.ForegroundColor = ConsoleColor.Green;
                Console.Write("> ");
                Console.ResetColor();
                start = DateTime.UtcNow;
                string input = Console.ReadLine() ?? "";
                end = DateTime.UtcNow;
                totalChars = input.Length;
                int minLen = Math.Min(text.Length, input.Length);
                errors = 0;
                for (int i = 0; i < minLen; i++) if (text[i] != input[i]) errors++;
                if (input.Length > text.Length) errors += input.Length - text.Length;
                double elapsed = (end - start).TotalSeconds;
                double cpm = (totalChars / elapsed) * 60;
                double wpm = cpm / 5;
                double accuracy = totalChars > 0 ? (double)(totalChars - errors) / totalChars * 100 : 0;
                Console.ForegroundColor = ConsoleColor.Cyan;
                Console.WriteLine("\n--- Результаты ---");
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"Скорость: {cpm:F1} симв/мин ({wpm:F1} слов/мин)");
                Console.ForegroundColor = ConsoleColor.Yellow;
                Console.WriteLine($"Точность: {accuracy:F1}%");
                Console.ForegroundColor = ConsoleColor.Magenta;
                Console.WriteLine($"Время: {elapsed:F2} сек");
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine($"Ошибок: {errors}");
                Console.ResetColor();
                return new Dictionary<string, object>
                {
                    ["cpm"] = cpm, ["wpm"] = wpm, ["accuracy"] = accuracy, ["time"] = elapsed, ["errors"] = errors
                };
            }
        }
    }
}
