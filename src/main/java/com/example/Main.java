/* 
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello Sans");
    }
}
*/
package com.example;
import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(3000);
            System.out.println("Server in ascolto sulla porta 3000...");

            // Il ServerSocket rimane aperto per tutta la durata del programma.
            // Il try-catch interno al ciclo gestisce i singoli client:
            // un errore su un client non ferma il server.
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connesso: " + clientSocket.getInetAddress());

                try {
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8")
                    );
                    PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true
                    );

                    // 1. Lettura della richiesta.
                    // Se il client si scollega senza inviare nulla, readLine() restituisce null.
                    String richiesta = in.readLine();
                    if (richiesta == null) {
                        System.out.println("  Client disconnesso senza inviare una richiesta.");
                        continue; // passo al client successivo
                    }

                    // 2. Analisi della richiesta.
                    // Controllo richiesta vuota prima di accedere all'array di split().
                    String richiestaTrim = richiesta.trim();
                    if (richiestaTrim.isEmpty()) {
                        inviaErrore(out, "ERR SYNTAX");
                    } else {
                        String[] parti = richiestaTrim.split("\\s+");
                        String comando = parti[0];

                        if (!comando.equals("LEGGI")) {
                            inviaErrore(out, "ERR UNKNOWNCMD");
                        } else if (parti.length != 2) {
                            inviaErrore(out, "ERR SYNTAX");
                        } else {
                            String nomeFile = parti[1];

                            if (!nomeFileValido(nomeFile)) {
                                inviaErrore(out, "ERR SYNTAX");
                            } else {
                                // 3. Ricerca e apertura del file PRIMA di comunicare OK FILE.
                                File file = new File("documenti/" + nomeFile);

                                if (!file.exists() || !file.isFile()) {
                                    inviaErrore(out, "ERR NOTFOUND");
                                } else {
                                    BufferedReader fileReader = null;
                                    try {
                                        fileReader = new BufferedReader(
                                            new InputStreamReader(new FileInputStream(file), "UTF-8")
                                        );

                                        // Solo ora, dopo aver aperto il file con successo,
                                        // invio lo stato di successo.
                                        out.println("OK FILE");
                                        out.println(); // riga vuota di separazione

                                        // 4. Trasmissione del contenuto riga per riga.
                                        String riga;
                                        while ((riga = fileReader.readLine()) != null) {
                                            out.println(riga);
                                        }
                                        // La fine del contenuto coincide con la chiusura del socket:
                                        // il client vedrà readLine() restituire null.

                                    } catch (IOException e) {
                                        // Errore DURANTE la lettura, dopo aver già inviato OK FILE.
                                        // Non posso inviare un secondo stato: registro e chiudo.
                                        System.err.println("  Errore durante la lettura del file '"
                                            + nomeFile + "': " + e.getMessage());
                                    } finally {
                                        if (fileReader != null) {
                                            try { fileReader.close(); } catch (IOException ignored) {}
                                        }
                                    }
                                }
                            }
                        }
                    }

                } catch (IOException e) {
                    // Errore di I/O sulla connessione con questo client:
                    // lo registro ma il server continua a funzionare.
                    System.err.println("  Errore nella gestione del client: " + e.getMessage());
                } finally {
                    // 5. Chiudo sempre il Socket del client, lasciando aperto il ServerSocket.
                    try { clientSocket.close(); } catch (IOException ignored) {}
                    System.out.println("  Connessione chiusa.");
                }
            }

        } catch (IOException e) {
            System.err.println("Impossibile avviare il server sulla porta 3000: " + e.getMessage());
        } finally {
            if (serverSocket != null) {
                try { serverSocket.close(); } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Verifica che il nome del file sia "semplice":
     * estensione .txt, nessun separatore di percorso, nessun "..".
     */
    private static boolean nomeFileValido(String nome) {
        if (!nome.endsWith(".txt")) return false;
        if (nome.length() <= 4) return false;           // solo ".txt" non va bene
        if (nome.contains("/") || nome.contains("\\") || nome.contains("..")) return false;
        return true;
    }

    /**
     * Invia una riga di stato seguita dalla riga vuota di separazione.
     */
    private static void inviaErrore(PrintWriter out, String codice) {
        out.println(codice);
        out.println();
    }
}