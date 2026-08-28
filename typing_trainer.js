#!/usr/bin/env node
// typing_trainer.js
const { program } = require('commander');
const readline = require('readline');
const chalk = require('chalk');
const fs = require('fs');

class TypingTrainer {
    constructor(length = 50, charset = 'all', mode = 'char', language = 'en') {
        this.length = length;
        this.mode = mode;
        this.language = language;
        this.charset = this._getCharset(charset);
        this.text = this._generateText();
        this.startTime = null;
        this.endTime = null;
        this.errors = 0;
        this.totalChars = 0;
        this.rl = readline.createInterface({ input: process.stdin, output: process.stdout });
    }

    _getCharset(charset) {
        let letters = this.language === 'ru' ?
            'абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ' :
            'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
        const digits = '0123456789';
        const symbols = '!@#$%^&*()_+-=,.;:?/[]{}|';
        if (charset === 'letters') return letters;
        if (charset === 'alnum') return letters + digits;
        return letters + digits + symbols;
    }

    _generateText() {
        if (this.mode === 'word') {
            const words = [];
            const count = Math.max(1, Math.floor(this.length / 5));
            for (let i = 0; i < count; i++) {
                const wl = 3 + Math.floor(Math.random() * 6);
                let word = '';
                for (let j = 0; j < wl; j++) word += this.charset[Math.floor(Math.random() * this.charset.length)];
                words.push(word);
            }
            return words.join(' ');
        } else if (this.mode === 'sentence') {
            const sentences = [];
            const count = Math.max(1, Math.floor(this.length / 30));
            for (let i = 0; i < count; i++) {
                const wc = 5 + Math.floor(Math.random() * 8);
                const words = [];
                for (let j = 0; j < wc; j++) {
                    const wl = 3 + Math.floor(Math.random() * 6);
                    let word = '';
                    for (let k = 0; k < wl; k++) word += this.charset[Math.floor(Math.random() * this.charset.length)];
                    words.push(word);
                }
                let sentence = words.join(' ');
                sentence = sentence.charAt(0).toUpperCase() + sentence.slice(1) + '.';
                sentences.push(sentence);
            }
            return sentences.join(' ');
        } else {
            let text = '';
            for (let i = 0; i < this.length; i++) {
                text += this.charset[Math.floor(Math.random() * this.charset.length)];
            }
            return text;
        }
    }

    async run() {
        console.log(chalk.cyan('Тренажер печати (символы)'));
        console.log(chalk.yellow('Введите следующий текст как можно точнее и быстрее:'));
        console.log(chalk.white(this.text));
        console.log('\nНажмите Enter, чтобы начать...');
        await this._question('');
        this.startTime = Date.now();
        const userInput = await this._question(chalk.green('> '));
        this.endTime = Date.now();
        this.totalChars = userInput.length;
        const minLen = Math.min(this.text.length, userInput.length);
        this.errors = 0;
        for (let i = 0; i < minLen; i++) {
            if (this.text[i] !== userInput[i]) this.errors++;
        }
        if (userInput.length > this.text.length) this.errors += userInput.length - this.text.length;
        const elapsed = (this.endTime - this.startTime) / 1000;
        const cpm = (this.totalChars / elapsed) * 60;
        const wpm = (this.totalChars / 5 / elapsed) * 60;
        const accuracy = this.totalChars > 0 ? ((this.totalChars - this.errors) / this.totalChars * 100) : 0;
        console.log(chalk.cyan('\n--- Результаты ---'));
        console.log(chalk.green(`Скорость: ${cpm.toFixed(1)} симв/мин (${wpm.toFixed(1)} слов/мин)`));
        console.log(chalk.yellow(`Точность: ${accuracy.toFixed(1)}%`));
        console.log(chalk.magenta(`Время: ${elapsed.toFixed(2)} сек`));
        console.log(chalk.red(`Ошибок: ${this.errors}`));
        this.rl.close();
        return { cpm, wpm, accuracy, time: elapsed, errors: this.errors };
    }

    _question(prompt) {
        return new Promise(resolve => this.rl.question(prompt, resolve));
    }
}

program
    .option('-l, --length <n>', 'Длина текста', parseInt, 50)
    .option('-c, --charset <set>', 'letters, alnum, all', 'all')
    .option('-m, --mode <mode>', 'char, word, sentence', 'char')
    .option('-L, --language <lang>', 'en, ru', 'en')
    .option('--export-stats <file>', 'Экспорт статистики')
    .parse(process.argv);

const opts = program.opts();
const trainer = new TypingTrainer(opts.length, opts.charset, opts.mode, opts.language);
trainer.run().then(stats => {
    if (opts.exportStats) {
        const ext = opts.exportStats.split('.').pop().toLowerCase();
        if (ext === 'json') fs.writeFileSync(opts.exportStats, JSON.stringify(stats, null, 2));
        else if (ext === 'csv') {
            const header = Object.keys(stats).join(',') + '\n';
            const row = Object.values(stats).join(',') + '\n';
            fs.writeFileSync(opts.exportStats, header + row);
        }
        console.log(`Статистика сохранена в ${opts.exportStats}`);
    }
});
