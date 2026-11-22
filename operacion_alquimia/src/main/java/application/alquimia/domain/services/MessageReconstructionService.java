package application.alquimia.domain.services;

import application.alquimia.domain.exceptions.MessageReconstructionException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MessageReconstructionService {

    /**
     * Reconstruye el mensaje original emitido por Alchemy combinando los fragmentos
     * interceptados por los dispositivos de vigilancia, eliminando ruidos y cadenas vacías ("").
     */
    public String reconstructMessage(List<List<String>> messages) {
        if (messages == null || messages.size() < 3) {
            throw new MessageReconstructionException("Se requieren los fragmentos de los 3 dispositivos para reconstruir el mensaje.");
        }

        // Determinar el tamaño mínimo común (en caso de desfases de recepción o prefijos de ruido)
        int minLength = Integer.MAX_VALUE;
        for (List<String> msg : messages) {
            if (msg == null || msg.isEmpty()) {
                throw new MessageReconstructionException("Uno de los fragmentos de mensaje está vacío o es nulo.");
            }
            minLength = Math.min(minLength, msg.size());
        }

        if (minLength == 0) {
            throw new MessageReconstructionException("No es posible reconstruir un mensaje de longitud cero.");
        }

        // Alinear los mensajes al tamaño final (tomando los últimos 'minLength' elementos de cada lista)
        List<List<String>> alignedMessages = new ArrayList<>();
        for (List<String> msg : messages) {
            int offset = msg.size() - minLength;
            alignedMessages.add(msg.subList(offset, msg.size()));
        }

        List<String> resultWords = new ArrayList<>();

        for (int i = 0; i < minLength; i++) {
            String wordAtPos = "";

            for (List<String> msg : alignedMessages) {
                String candidate = msg.get(i);
                if (candidate != null && !candidate.trim().isEmpty()) {
                    if (wordAtPos.isEmpty()) {
                        wordAtPos = candidate.trim();
                    } else if (!wordAtPos.equalsIgnoreCase(candidate.trim())) {
                        throw new MessageReconstructionException(String.format(
                                "Conflicto o interferencia en la palabra en posición %d: '%s' vs '%s'",
                                i, wordAtPos, candidate
                        ));
                    }
                }
            }

            if (wordAtPos.isEmpty()) {
                throw new MessageReconstructionException(String.format(
                        "Información insuficiente: la palabra en la posición %d no pudo ser recuperada por ningún dispositivo.",
                        i
                ));
            }

            resultWords.add(wordAtPos);
        }

        return String.join(" ", resultWords);
    }
}
