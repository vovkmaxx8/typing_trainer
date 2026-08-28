// typing_trainer.rs
use clap::{App, Arg};
use rand::prelude::*;
use std::io::{self, Write, BufRead};
use std::time::Instant;
use colored::*;
use serde_json;
use std::fs;

struct Trainer {
    length: usize,
    charset: String,
    mode: String,
    language: String,
    text: String,
}

impl Trainer {
    fn new(length: usize, charset: &str, mode: &str, language: &str) -> Self {
        let mut t = Trainer { length, charset: charset.to_string(), mode: mode.to_string(), language: language.to_string(), text: String::new() };
        t.text = t.generate_text();
        t
    }

    fn get_charset(&self) -> String {
        let letters = if self.language == "ru" {
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
        } else {
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        };
        let digits = "0123456789";
        let symbols = "!@#$%^&*()_+-=,.;:?/[]{}|";
        match self.charset.as_str() {
            "letters" => letters.to_string(),
            "alnum" => format!("{}{}", letters, digits),
            _ => format!("{}{}{}", letters, digits, symbols),
        }
    }

    fn generate_text(&self) -> String {
        let charset = self.get_charset();
        let mut rng = thread_rng();
        if self.mode == "word" {
            let word_count = std::cmp::max(1, self.length / 5);
            let mut words = Vec::new();
            for _ in 0..word_count {
                let wl = rng.gen_range(3..=8);
                let word: String = (0..wl).map(|_| {
                    let idx = rng.gen_range(0..charset.len());
                    charset.chars().nth(idx).unwrap()
                }).collect();
                words.push(word);
            }
            words.join(" ")
        } else if self.mode == "sentence" {
            let sent_count = std::cmp::max(1, self.length / 30);
            let mut sentences = Vec::new();
            for _ in 0..sent_count {
                let wc = rng.gen_range(5..=12);
                let mut words = Vec::new();
                for _ in 0..wc {
                    let wl = rng.gen_range(3..=8);
                    let word: String = (0..wl).map(|_| {
                        let idx = rng.gen_range(0..charset.len());
                        charset.chars().nth(idx).unwrap()
                    }).collect();
                    words.push(word);
                }
                let mut sent = words.join(" ");
                // capitalize
                let first = sent.chars().next().unwrap().to_uppercase().to_string();
                sent = first + &sent[1..];
                sent.push('.');
                sentences.push(sent);
            }
            sentences.join(" ")
        } else {
            let mut res = String::new();
            for _ in 0..self.length {
                let idx = rng.gen_range(0..charset.len());
                res.push(charset.chars().nth(idx).unwrap());
            }
            res
        }
    }

    fn run(&self) -> serde_json::Value {
        let stdin = io::stdin();
        let mut stdout = io::stdout();
        println!("{}", "Тренажер печати (символы)".cyan());
        println!("{}", "Введите следующий текст как можно точнее и быстрее:".yellow());
        println!("{}", self.text.white());
        println!("\nНажмите Enter, чтобы начать...");
        let mut line = String::new();
        stdin.read_line(&mut line).unwrap();
        print!("{}", "> ".green());
        stdout.flush().unwrap();
        let start = Instant::now();
        line.clear();
        stdin.read_line(&mut line).unwrap();
        let input = line.trim();
        let elapsed = start.elapsed().as_secs_f64();
        let total = input.len();
        let mut errors = 0;
        let min_len = std::cmp::min(self.text.len(), input.len());
        for i in 0..min_len {
            if self.text.chars().nth(i) != input.chars().nth(i) {
                errors += 1;
            }
        }
        if input.len() > self.text.len() {
            errors += input.len() - self.text.len();
        }
        let cpm = if elapsed > 0.0 { (total as f64 / elapsed) * 60.0 } else { 0.0 };
        let wpm = cpm / 5.0;
        let accuracy = if total > 0 { ((total - errors) as f64 / total as f64) * 100.0 } else { 0.0 };
        println!("\n{}", "--- Результаты ---".cyan());
        println!("{}", format!("Скорость: {:.1} симв/мин ({:.1} слов/мин)", cpm, wpm).green());
        println!("{}", format!("Точность: {:.1}%", accuracy).yellow());
        println!("{}", format!("Время: {:.2} сек", elapsed).magenta());
        println!("{}", format!("Ошибок: {}", errors).red());

        json!({
            "cpm": cpm,
            "wpm": wpm,
            "accuracy": accuracy,
            "time": elapsed,
            "errors": errors
        })
    }
}

fn main() {
    let matches = App::new("Typing Trainer")
        .arg(Arg::with_name("length").long("length").takes_value(true).default_value("50"))
        .arg(Arg::with_name("charset").long("charset").takes_value(true).default_value("all"))
        .arg(Arg::with_name("mode").long("mode").takes_value(true).default_value("char"))
        .arg(Arg::with_name("language").long("language").takes_value(true).default_value("en"))
        .arg(Arg::with_name("export-stats").long("export-stats").takes_value(true))
        .get_matches();

    let length: usize = matches.value_of("length").unwrap().parse().unwrap();
    let charset = matches.value_of("charset").unwrap();
    let mode = matches.value_of("mode").unwrap();
    let language = matches.value_of("language").unwrap();
    let trainer = Trainer::new(length, charset, mode, language);
    let stats = trainer.run();
    if let Some(file) = matches.value_of("export-stats") {
        let json = serde_json::to_string_pretty(&stats).unwrap();
        fs::write(file, json).unwrap();
        println!("Статистика сохранена в {}", file);
    }
}
