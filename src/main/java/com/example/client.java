import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Client avviato. Connettersi al server sulla porta 3000.");

        while (true) {
            System.out.print("\nInserisci il nome del file (o 'esci' per terminare): ");
            String input = scanner.nextLine().trim();

            // Condizione di uscita dal ciclo (solo lato client)
            if (input.equalsIgnoreCase("esci") || input.equalsIgnoreCase("quit")) {
                System.out.println("Chiusura del client.");
                break;
            }

            if (input.isEmpty()) {
                System.out.println("Il nome del file non può essere vuoto.");
                continue;
            }

            // Try-with-resources: garantisce che il socket venga chiuso 
            // alla fine di ogni iterazione, anche in caso di eccezioni.
            try (Socket socket = new Socket("localhost", 3000)) {
                
                // Configurazione stream con UTF-8
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                );
                PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true
                );

                // 1. Invio della richiesta
                out.println("LEGGI " + input);

                // 2. Lettura della riga di stato
                String stato = in.readLine();
                if (stato == null) {
                    System.out.println("Errore: risposta incompleta dal server (nessuno stato ricevuto).");
                    continue;
                }

                // 3. Lettura della riga vuota di separazione
                String separatore = in.readLine();
                if (separatore == null) {
                    System.out.println("Errore: risposta incompleta dal server (nessun separatore ricevuto).");
                    continue;
                }
                
                // Controllo di conformità del separatore
                if (!separatore.isEmpty()) {
                    System.out.println("Attenzione: risposta non conforme alle regole (il separatore non è una riga vuota).");
                }

                // 4. Elaborazione della risposta
                if ("OK FILE".equals(stato)) {
                    System.out.println("--- Inizio contenuto del file ---");
                    String riga;
                    // Legge fino a null (che indica la chiusura del socket da parte del server)
                    while ((riga = in.readLine()) != null) {
                        System.out.println(riga);
                    }
                    System.out.println("--- Fine contenuto del file ---");
                } else {
                    // Gestione degli errori con messaggi comprensibili per l'utente
                    System.out.println("Operazione fallita. Codice errore: " + stato);
                    switch (stato) {
                        case "ERR SYNTAX":
                            System.out.println("Motivo: Richiesta non valida. Usa il formato: LEGGI nomefile.txt");
                            break;
                        case "ERR UNKNOWNCMD":
                            System.out.println("Motivo: Comando sconosciuto. Il server accetta solo 'LEGGI'.");
                            break;
                        case "ERR NOTFOUND":
                            System.out.println("Motivo: Il file '" + input + "' non esiste nella cartella 'documenti'.");
                            break;
                        case "ERR READ":
                            System.out.println("Motivo: Il file esiste, ma il server non è riuscito ad aprirlo in lettura.");
                            break;
                        default:
                            System.out.println("Motivo: Errore sconosciuto dal server.");
                    }
                }

            } catch (ConnectException e) {
                System.out.println("Errore di connessione: Impossibile raggiungere il server sulla porta 3000. Assicurati che sia in esecuzione.");
            } catch (IOException e) {
                System.out.println("Errore di comunicazione: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
}