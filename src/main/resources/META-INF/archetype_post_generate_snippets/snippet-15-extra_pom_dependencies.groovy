// -----------------------------------------------------------------------------




// SNIPPET-15-EXTRA_POM_DEPENDENCIES.GROOVY ------------------------------------
// OWNER: the extra dependencies injected into the generated pom.xml.
// An absent flag becomes the string 'null' and the catalog also accepts
// 'false' as "no dependency": neither reaches the injection below.
if (extra_pom_dependencies != null &&
        extra_pom_dependencies.toString().trim().length() > 0 &&
        extra_pom_dependencies.toString() != 'false' &&
        extra_pom_dependencies.toString() != 'null') {

    def additional_dependencies_splitter = ','
    def dependency_internal_coordinates_splitter = ':'

    // REGEX will avoid any 'SPACE' in the input storaged in 'extra_pom_dependencies variable'
    // JUST IN CASE, the code below will be executed
    extra_pom_dependencies = extra_pom_dependencies.trim()
    extra_pom_dependencies = extra_pom_dependencies.replaceAll("\\s+", "").trim()

    String[] dependencies = extra_pom_dependencies.split(additional_dependencies_splitter)

    Path pomFilePath =
            Paths.get(request.outputDirectory,
                    request.artifactId,
                    'pom.xml')

    log_text += "\nPOM.XML"
    log_text += "\n - Location: $pomFilePath"
    log_text += "\n - Added Dependencies: $dependencies\n"

    if (pomFilePath.toFile().isFile()) {

        def sb = new StringBuilder()

        Scanner sc

        try {
            sc = new Scanner(pomFilePath.toFile(), "UTF-8")
            String currentLine = ''
            String lastLine = ''

            while (sc.hasNext()) {

                lastLine = currentLine
                currentLine = sc.nextLine()

                if (currentLine.trim().length() == 0) continue

                if (currentLine.contains("</dependency>") ||
                        currentLine.contains("</modelVersion>") ||
                        currentLine.contains("</parent>") ||
                        currentLine.contains("</description>") ||
                        currentLine.contains("</properties>") ||
                        currentLine.contains("</dependencies>") ||
                        currentLine.contains("</build>") ||
                        currentLine.contains("</dependencyManagement>")) sb.append(currentLine).append("\n\n")
                else sb.append(currentLine).append("\n")

                // Add dependencies
                if (currentLine.contains("<dependencies>")) {

                    for (def dependency : dependencies) {

                        def splitter = dependency_internal_coordinates_splitter

                        String[] depInfo = dependency.split(splitter)

                        // a coordinate without group, artifact and version is
                        // reported and skipped - it must not break the
                        // post-processor
                        if (depInfo.length < 3) {
                            log_text += "\n - Ignored malformed dependency: ${dependency}\n"
                            continue
                        }

                        String scopeVal = 'compile'

                        if (depInfo.length > 3) scopeVal = depInfo[3]

                        sb.append('        <dependency>').append('\n')
                        sb.append("            <groupId>${depInfo[0]}</groupId>").append('\n')
                        sb.append("            <artifactId>${depInfo[1]}</artifactId>").append('\n')
                        sb.append("            <version>${depInfo[2]}</version>").append('\n')
                        sb.append("            <scope>${scopeVal}</scope>").append('\n')
                        sb.append('        </dependency>').append('\n\n')
                    }
                }
            }

        } finally {
            sc.close()
        }

        // Write the modified parent pom
        PrintWriter writer = new PrintWriter(pomFilePath.toFile(), "UTF-8")
        writer.write(sb.toString())
        writer.close()
    }
}