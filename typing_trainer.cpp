// typing_trainer.cpp
#include <iostream>
#include <string>
#include <vector>
#include <random>
#include <chrono>
#include <thread>
#include <algorithm>
#include <ctime>
#include <iomanip>

using namespace std;

class Trainer {
private:
    int length;
    string charset, mode, language;
    string text;
    int errors;
    int totalChars;
    chrono::steady_clock::time_point start, end;

    string getCharset() {
        string letters = (language == "ru") ?
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" :
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        string digits = "0123456789";
        string symbols = "!@#$%^&*()_+-=,.;:?/[]{}|";
        if (charset == "letters") return letters;
        if (charset == "alnum") return letters + digits;
        return letters + digits + symbols;
    }

    string generateText() {
        string cs = getCharset();
        random_device rd;
        mt19937 gen(rd());
        uniform_int_distribution<> dist(0, cs.size()-1);
        if (mode == "word") {
            int wordCount = max(1, length / 5);
            vector<string> words;
            for (int i = 0; i < wordCount; ++i) {
                int wl = 3 + (gen() % 6);
                string word;
                for (int j = 0; j < wl; ++j) word += cs[dist(gen)];
                words.push_back(word);
            }
            string result;
            for (size_t i = 0; i < words.size(); ++i) {
                if (i) result += " ";
                result += words[i];
            }
            return result;
        } else if (mode == "sentence") {
            int sentCount = max(1, length / 30);
            vector<string> sentences;
            for (int i = 0; i < sentCount; ++i) {
                int wc = 5 + (gen() % 8);
                vector<string> words;
                for (int j = 0; j < wc; ++j) {
                    int wl = 3 + (gen() % 6);
                    string word;
                    for (int k = 0; k < wl; ++k) word += cs[dist(gen)];
                    words.push_back(word);
                }
                string sent;
                for (size_t j = 0; j < words.size(); ++j) {
                    if (j) sent += " ";
                    sent += words[j];
                }
                if (!sent.empty()) sent[0] = toupper(sent[0]);
                sent += ".";
                sentences.push_back(sent);
            }
            string result;
            for (size_t i = 0; i < sentences.size(); ++i) {
                if (i) result += " ";
                result += sentences[i];
            }
            return result;
        } else {
            string result;
            for (int i = 0; i < length; ++i) result += cs[dist(gen)];
            return result;
        }
    }

public:
    Trainer(int len, const string& cs, const string& md, const string& lang)
        : length(len), charset(cs), mode(md), language(lang) {
        text = generateText();
        errors = 0;
        totalChars = 0;
    }

    void run() {
        cout << "\033[36mТренажер печати (символы)\033[0m" << endl;
        cout << "\033[33mВведите следующий текст как можно точнее и быстрее:\033[0m" << endl;
        cout << "\033[37m" << text << "\033[0m" << endl;
        cout << "\nНажмите Enter, чтобы начать...";
        cin.ignore();
        start = chrono::steady_clock::now();
        cout << "\033[32m> \033[0m";
        string input;
        getline(cin, input);
        end = chrono::steady_clock::now();
        totalChars = input.size();
        int minLen = min(text.size(), input.size());
        errors = 0;
        for (int i = 0; i < minLen; ++i) {
            if (text[i] != input[i]) ++errors;
        }
        if (input.size() > text.size()) errors += input.size() - text.size();
        double elapsed = chrono::duration<double>(end - start).count();
        double cpm = (totalChars / elapsed) * 60;
        double wpm = cpm / 5;
        double accuracy = totalChars > 0 ? (double)(totalChars - errors) / totalChars * 100 : 0;
        cout << "\n\033[36m--- Результаты ---\033[0m" << endl;
        cout << "\033[32mСкорость: " << fixed << setprecision(1) << cpm << " симв/мин (" << wpm << " слов/мин)\033[0m" << endl;
        cout << "\033[33mТочность: " << accuracy << "%\033[0m" << endl;
        cout << "\033[35mВремя: " << elapsed << " сек\033[0m" << endl;
        cout << "\033[31mОшибок: " << errors << "\033[0m" << endl;
    }
};

int main(int argc, char* argv[]) {
    int length = 50;
    string charset = "all", mode = "char", language = "en", exportFile;

    for (int i = 1; i < argc; ++i) {
        string arg = argv[i];
        if (arg == "--length" && i+1 < argc) length = stoi(argv[++i]);
        else if (arg == "--charset" && i+1 < argc) charset = argv[++i];
        else if (arg == "--mode" && i+1 < argc) mode = argv[++i];
        else if (arg == "--language" && i+1 < argc) language = argv[++i];
        else if (arg == "--export-stats" && i+1 < argc) exportFile = argv[++i];
    }

    Trainer trainer(length, charset, mode, language);
    trainer.run();
    // Экспорт в C++ не реализован для краткости
    return 0;
}
