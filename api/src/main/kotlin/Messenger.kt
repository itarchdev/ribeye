package ru.it_arch.tools.samples.ribeye

/**
 * Простой месенджер для отправки текстовых сообщений во внешнюю среду
 * */
public interface Messenger {
    /**
     * Отправка сообщения.
     *
     * @param msg текст сообщения
     * */
    public suspend fun send(msg: String)
}
