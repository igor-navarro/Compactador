import java.io.IOException;
import java.nio.file.*;
import java.util.zip.*;

public class Main {

    public static void compactar(Path pasta, Path destino) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(destino));
             var arquivos = Files.walk(pasta)) {

            arquivos.filter(Files::isRegularFile).forEach(arquivo -> {
                try {
                    String nome = pasta.relativize(arquivo).toString().replace("\\", "/");
                    zos.putNextEntry(new ZipEntry(nome));
                    Files.copy(arquivo, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    System.err.println("Erro em " + arquivo + ": " + e.getMessage());
                }
            });
        }
    }

    public static void extrair(Path zip, Path destino) throws IOException {
        Path base = destino.toAbsolutePath().normalize();
        Files.createDirectories(base);

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entrada;
            while ((entrada = zis.getNextEntry()) != null) {
                Path saida = base.resolve(entrada.getName()).normalize();

                // Proteção contra Zip Slip
                if (!saida.startsWith(base)) {
                    throw new IOException("Entrada inválida: " + entrada.getName());
                }

                if (entrada.isDirectory()) {
                    Files.createDirectories(saida);
                } else {
                    Files.createDirectories(saida.getParent());
                    Files.copy(zis, saida, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Uso:");
            System.out.println("  java Main compactar <pasta> <arquivo.zip>");
            System.out.println("  java Main extrair <arquivo.zip> <pasta_destino>");
            return;
        }

        switch (args[0]) {
            case "compactar" -> {
                compactar(Path.of(args[1]), Path.of(args[2]));
                System.out.println("Compactado com sucesso!");
            }
            case "extrair" -> {
                extrair(Path.of(args[1]), Path.of(args[2]));
                System.out.println("Extraído com sucesso!");
            }
            default -> System.out.println("Comando desconhecido: " + args[0]);
        }
    }
}