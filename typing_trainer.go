// typing_trainer.go
package main

import (
	"bufio"
	"encoding/csv"
	"encoding/json"
	"flag"
	"fmt"
	"math/rand"
	"os"
	"strings"
	"time"
)

type Trainer struct {
	length   int
	charset  string
	mode     string
	language string
	text     string
	start    time.Time
	end      time.Time
	errors   int
	total    int
}

func NewTrainer(length int, charset, mode, language string) *Trainer {
	t := &Trainer{length: length, charset: charset, mode: mode, language: language}
	t.text = t.generateText()
	return t
}

func (t *Trainer) getCharset() string {
	var letters string
	if t.language == "ru" {
		letters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
	} else {
		letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
	}
	digits := "0123456789"
	symbols := "!@#$%^&*()_+-=,.;:?/[]{}|"
	switch t.charset {
	case "letters":
		return letters
	case "alnum":
		return letters + digits
	default:
		return letters + digits + symbols
	}
}

func (t *Trainer) generateText() string {
	charset := t.getCharset()
	if t.mode == "word" {
		count := len(charset)
		words := []string{}
		wordCount := max(1, t.length/5)
		for i := 0; i < wordCount; i++ {
			wl := rand.Intn(6) + 3
			word := ""
			for j := 0; j < wl; j++ {
				word += string(charset[rand.Intn(count)])
			}
			words = append(words, word)
		}
		return strings.Join(words, " ")
	} else if t.mode == "sentence" {
		sentences := []string{}
		sentenceCount := max(1, t.length/30)
		for i := 0; i < sentenceCount; i++ {
			wc := rand.Intn(8) + 5
			words := []string{}
			for j := 0; j < wc; j++ {
				wl := rand.Intn(6) + 3
				word := ""
				for k := 0; k < wl; k++ {
					word += string(charset[rand.Intn(len(charset))])
				}
				words = append(words, word)
			}
			sentence := strings.Join(words, " ")
			sentence = strings.ToUpper(sentence[:1]) + sentence[1:] + "."
			sentences = append(sentences, sentence)
		}
		return strings.Join(sentences, " ")
	} else {
		res := ""
		for i := 0; i < t.length; i++ {
			res += string(charset[rand.Intn(len(charset))])
		}
		return res
	}
}

func (t *Trainer) Run() map[string]interface{} {
	reader := bufio.NewReader(os.Stdin)
	fmt.Println("\033[36mТренажер печати (символы)\033[0m")
	fmt.Println("\033[33mВведите следующий текст как можно точнее и быстрее:\033[0m")
	fmt.Println("\033[37m" + t.text + "\033[0m")
	fmt.Println("\nНажмите Enter, чтобы начать...")
	reader.ReadString('\n')
	t.start = time.Now()
	fmt.Print("\033[32m> \033[0m")
	input, _ := reader.ReadString('\n')
	input = strings.TrimSuffix(input, "\n")
	t.end = time.Now()
	t.total = len(input)
	minLen := len(t.text)
	if len(input) < minLen {
		minLen = len(input)
	}
	t.errors = 0
	for i := 0; i < minLen; i++ {
		if t.text[i] != input[i] {
			t.errors++
		}
	}
	if len(input) > len(t.text) {
		t.errors += len(input) - len(t.text)
	}
	elapsed := t.end.Sub(t.start).Seconds()
	cpm := float64(t.total) / elapsed * 60
	wpm := cpm / 5
	accuracy := 0.0
	if t.total > 0 {
		accuracy = float64(t.total-t.errors) / float64(t.total) * 100
	}
	fmt.Println("\n\033[36m--- Результаты ---\033[0m")
	fmt.Printf("\033[32mСкорость: %.1f симв/мин (%.1f слов/мин)\033[0m\n", cpm, wpm)
	fmt.Printf("\033[33mТочность: %.1f%%\033[0m\n", accuracy)
	fmt.Printf("\033[35mВремя: %.2f сек\033[0m\n", elapsed)
	fmt.Printf("\033[31mОшибок: %d\033[0m\n", t.errors)
	return map[string]interface{}{
		"cpm": cpm, "wpm": wpm, "accuracy": accuracy, "time": elapsed, "errors": t.errors,
	}
}

func max(a, b int) int {
	if a > b {
		return a
	}
	return b
}

func main() {
	var (
		length   int
		charset  string
		mode     string
		language string
		export   string
	)
	flag.IntVar(&length, "length", 50, "Длина текста")
	flag.StringVar(&charset, "charset", "all", "letters, alnum, all")
	flag.StringVar(&mode, "mode", "char", "char, word, sentence")
	flag.StringVar(&language, "language", "en", "en, ru")
	flag.StringVar(&export, "export-stats", "", "Экспорт статистики")
	flag.Parse()

	rand.Seed(time.Now().UnixNano())
	trainer := NewTrainer(length, charset, mode, language)
	stats := trainer.Run()
	if export != "" {
		ext := export[strings.LastIndex(export, ".")+1:]
		var data []byte
		var err error
		if ext == "json" {
			data, err = json.MarshalIndent(stats, "", "  ")
		} else if ext == "csv" {
			var line string
			for k, v := range stats {
				line += fmt.Sprintf("%v,", v)
			}
			line = line[:len(line)-1] + "\n"
			data = []byte(line)
		}
		if err == nil {
			os.WriteFile(export, data, 0644)
			fmt.Printf("Статистика сохранена в %s\n", export)
		}
	}
}
