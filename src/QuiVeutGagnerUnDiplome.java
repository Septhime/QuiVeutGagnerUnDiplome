import extensions.CSVFile;

class QuiVeutGagnerUnDiplome extends Program {

    int screenlength = 156; // Longueur de l'écran pour 1080p (1920x1080) avec zoom à 125% 

    int maxquestions; // Nombre de questions dans le niveau
    int[] removedquestions; // Liste des questions supprimées
    int nbremovedquestions; // Nombre de questions supprimées
    String nameLevel; // Nom du niveau
    String periodname; // Nom des périodes

    void algorithm() { // Fonction principale
        int choice = menuprincipal();
        if (choice == 1) { // Nouvelle partie
            String levelSelected = choixNiveau();
            if (equals(levelSelected, "return")) {
                algorithm();
            } else {
                int choiceMode = ChoixMode();
                if (choiceMode != 5 && rightLevel(levelSelected,choiceMode)) {
                    nameLevel = levelSelected;
                    String[] rawParam = getParametersFromLevel(levelSelected);
                    int[] parameters = new int[]{stringToInt(rawParam[0]), stringToInt(rawParam[1]), stringToInt(rawParam[2])};
                    Question[] questions = getQuestionsFromLevel(levelSelected);

                    periodname = rawParam[3];
                    nbremovedquestions = 0;
                    maxquestions = length(questions);
                    removedquestions = new int[maxquestions];

                    if (choiceMode == 1) { // Mode Normal
                        runNormalModeLevel(questions, parameters, 0);
                    } else if (choiceMode == 2) { // Mode Survie
                        runSurvieModeLevel(questions);
                    } else if (choiceMode == 3) { // Mode Burger de la Mort
                        runBurgerModeLevel(questions);
                    } else if (choiceMode == 4) { // Mode Mort Subite
                        runMortSubiteModeLevel(questions);
                    }
                } else {
                    algorithm();
                }
            }
        } else if (choice == 2) { // Charger partie
            String saveSelected = ChoixSauvegarde();
            if (equals(saveSelected, "return")) {
                algorithm();
            } else if (rightSave(saveSelected)) { // Si la sauvegarde est la bonne
                CSVFile savefile = loadCSV("ressources/saves/" + saveSelected,';'); // Fichier de la sauvegarde sélectionnée
                String levelFromSave = getCell(savefile, 0, 1); // Nom du niveau de la sauvegarde

                Question[] questions = getQuestionsFromLevel(levelFromSave); // Questions du niveau
                removedquestions = new int[length(questions)]; // Liste des questions supprimées
                nbremovedquestions = 0;
                int noQuestionToRemove;

                maxquestions = length(questions);
                for (int x = 0; x<columnCount(savefile); x++) { // Boucle pour supprimer les questions de la sauvegarde
                    if (!equals(getCell(savefile, 1, x), "null")) {
                        noQuestionToRemove = stringToInt(getCell(savefile, 1, x));
                        removeQuestionWithNoQuestionFromList(questions, noQuestionToRemove); // Supprime les questions de la sauvegarde
                        removedquestions[x] = noQuestionToRemove; // Ajoute les questions supprimées à la liste
                        nbremovedquestions++;
                        maxquestions--;
                    }
                }
                String[] param = getParametersFromLevel(levelFromSave); // Paramètres de la sauvegarde

                periodname = param[3];
                nameLevel = levelFromSave;

                runNormalModeLevel(questions, new int[]{stringToInt(param[0]),stringToInt(param[1]),stringToInt(param[2])}, stringToInt(getCell(savefile, 0, 2)));
            } else {
                algorithm();
            }
        } else if (choice == 3) { // Crédits
            showHeader();
            centerText("Fait par :");
            centerText("Sulivan Cerdan");
            centerText("Timothée Varin");
            centerText("BUT INFO S1");
            centerText("Groupe F");
            print("\n\n\n");
            waitinputuser();
            algorithm();
        }
    }

    // Fonctions pour l'interface

    void centerText(String text) { // Centre un texte
        int x = (screenlength - length(text))/2;
        for (int j = 0; j < x; j++) {
            print(" ");
        }
        print(text);
        print("\n");
    }

    void showHeader() { // Affiche l'en-tête
        print(ANSI_CLEAR_SCREEN_ALL);
        showTextFromCSV("logo");
        print("\n\n\n");
    }

    void showTextFromCSV(String filename) { // Affiche un texte depuis un fichier CSV
        CSVFile file = loadCSV("ressources/text/"+filename+".csv",';');
        for (int x = 0; x<rowCount(file); x++) {
            centerText((getCell(file, x, 0)));
        }
    }

    String choixNiveau(){ // Affiche le menu de choix de niveau et récupère le choix de l'utilisateur
        String[] files = getAllFilesFromDirectory("ressources/levels");

        showHeader();
        centerText("Choisissez un niveau");
        print("\n\n");
        String name;
        for (int i = 0; i < length(files); i++) {
            if (equals(substring(files[i], length(files[i])-4, length(files[i])), ".csv")) {
                name = (i+1) + ".  ";
                String levelName = getLevelNameFromFile(files[i]);
                if (length(levelName) > 13) { // Si le nom du niveau est trop long
                    name = name + substring(levelName, 0, 10) + "...";
                } else {
                    name = name + levelName;
                }
                if (length(name) < 17) { 
                    int length = length(name);
                    for (int x = 0; x < 17-length; x++) {
                        name = name + " ";
                    }
                }
                centerText(name);
            }
        }
        centerText("9.  <- retour    ");
        print("\n\n");
        int input = getinputuser(9);
        if (input == 9) {
            return "return";
        }
        if (input > length(files) && input != 9) {
            return choixNiveau();
        } else {
            return files[input-1];
        }
    }

    int ChoixMode() { // Affiche le menu de choix de mode de jeu et récupère la réponse de l'utilisateur
        showHeader();
        centerText("Choisissez un mode de jeu");
        print("\n\n");
        centerText("1. Mode Normal           ");
        centerText("2. Mode Survie           ");
        centerText("3. Mode Burger de la Mort");
        centerText("4. Mode Mort Subite      ");
        centerText("5. <- retour             ");
        print("\n\n");
        return getinputuser(5);
    }
    
    String ChoixSauvegarde(){ // Affiche le menu de choix de sauvegarde et récupère la réponse de l'utilisateur
        String[] files = getAllFilesFromDirectory("ressources/saves");

        showHeader();
        centerText("Choisissez une partie");
        print("\n\n");
        String name;
        for (int i = 0; i < length(files); i++) {
            if (equals(substring(files[i], length(files[i])-4, length(files[i])), ".csv")) {
                name = (i+1) + ".  ";
                String saveName = getSaveNameFromFile(files[i]);
                if (length(saveName) > 13) {
                    name = name + substring(saveName, 0, 10) + "...";
                } else {
                    name = name + saveName;
                }
                if (length(name) < 17) {
                    int length = length(name);
                    for (int x = 0; x < 17-length; x++) {
                        name = name + " ";
                    }
                }
                centerText(name);
            }
        }
        centerText("9.  <- retour    ");
        print("\n\n");
        int input = getinputuser(9);
        if (input == 9) {
            return "return";
        }
        if (input > length(files) && input != 9) {
            return ChoixSauvegarde();
        } else {
            return files[input-1];
        }
    }

    void waitinputuser() { // Attend que l'utilisateur appuie sur une touche
        print("Appuyez sur \"Entrée\" pour continuer...");
        readString();
        println();
    }

    int getinputuser(int max) { // Récupère une valeur de l'utilisateur entre 1 et max
        String entryuser;
        char entryuserchar;
        int entryuserint;
        do {
            print("Entrez un nombre entre 1 et " + max + " : ");
            entryuser = readString();
            if (entryuser == "") {
                entryuserint = 0;
            } else {
                entryuserchar = charAt(entryuser, 0);
                if (entryuserchar > '0' && entryuserchar <= '9') {
                    entryuserint = charToInt(entryuserchar);
                } else {
                    entryuserint = 0;
                }
            } 
        } while (entryuserint < 1 || entryuserint > max);
        return entryuserint;
    }

    void showquestion(Question question, int nbquestion) { // Affiche une question
        String[] answer = question.reponses;
        int nbanswer = 0;

        for (int x = 0; x<length(answer); x++) {
            if (!equals(answer[x], "null")) {
                nbanswer++;
            }
        }

        showHeader();
        centerText("Question " + nbquestion);
        print("\n\n");
        centerText(question.question);
        print("\n\n");
        for (int x = 0; x < nbanswer; x++) {
            centerText((x+1) + ") " + answer[x]);
        }
        print("\n\n");
    }


    boolean askquestion(Question question, int nbquestion, int nbperiod) { // Affiche une question et récupère la réponse de l'utilisateur
        String[] answer = question.reponses;
        int nbanswer = 0;

        for (int x = 0; x<length(answer); x++) {
            if (!equals(answer[x], "null")) {
                nbanswer++;
            }
        }

        showHeader();
        centerText(toUpperCase(""+charAt(periodname, 0)) + substring(periodname, 1, length(periodname)) + " " + nbperiod);
        centerText("Question " + nbquestion);
        print("\n\n");
        centerText(question.question);
        print("\n\n");
        for (int x = 0; x < nbanswer; x++) {
            centerText((x+1) + ") " + answer[x]);
        }
        print("\n\n");

        int userinput = getinputuser(nbanswer);

        showHeader();
        centerText(toUpperCase(""+charAt(periodname, 0)) + substring(periodname, 1, length(periodname)) + " " + nbperiod);
        centerText("Question " + nbquestion);
        print("\n\n");
        centerText(question.question);
        print("\n\n");

        if (userinput == question.bonneReponse) { // Si la réponse est correcte
            for (int x = 0; x < nbanswer; x++) {
                if (x == userinput-1) {
                    text("green");
                    centerText((x+1) + ") " + answer[x]);
                    text("white");
                } else {
                    text("red");
                    centerText((x+1) + ") " + answer[x]);
                    text("white");
                }
            }
            return true;
        } else {
            for (int x = 0; x < nbanswer; x++) { // Si la réponse est incorrecte
                if (x == userinput-1) {
                    text("red");
                    centerText((x+1) + ") " + answer[x]);
                    text("white");
                } else {
                    centerText((x+1) + ") " + answer[x]);
                }
            }
            return false;
        }
    }

    boolean askquestion(Question question, int nbquestion, int hp, int maxhp) { // Affiche une question et récupère la réponse de l'utilisateur
        String[] answer = question.reponses;
        int nbanswer = 0;

        for (int x = 0; x<length(answer); x++) {
            if (!equals(answer[x], "null")) {
                nbanswer++;
            }
        }

        showHeader();
        centerText("Question " + nbquestion);
        print("\n\n");
        centerText(question.question);
        print("\n\n");
        for (int x = 0; x < nbanswer; x++) {
            centerText((x+1) + ") " + answer[x]);
        }
        print("\n");
        centerText("HP " + hp + "/" + maxhp);
        print("\n");

        int userinput = getinputuser(nbanswer);

        showHeader();
        centerText("Question " + nbquestion);
        print("\n\n");
        centerText(question.question);
        print("\n\n");
        boolean correctAnswer;
        if (userinput == question.bonneReponse) { // Si la réponse est correcte
            for (int x = 0; x < nbanswer; x++) {
                if (x == userinput-1) {
                    text("green");
                    centerText((x+1) + ") " + answer[x]);
                    text("white");
                } else {
                    text("red");
                    centerText((x+1) + ") " + answer[x]);
                    text("white");
                }
            }
            correctAnswer = true;
        } else {
            for (int x = 0; x < nbanswer; x++) { // Si la réponse est incorrecte
                if (x == userinput-1) {
                    text("red");
                    centerText((x+1) + ") " + answer[x]);
                    text("white");
                } else {
                    centerText((x+1) + ") " + answer[x]);
                }
            }
            correctAnswer = false;
            hp--;
        } 
        print("\n");
        centerText("HP " + hp + "/" + maxhp);
        return correctAnswer;
    }

    boolean askquestion(Question question, int nbquestion) { // Affiche une question et récupère la réponse de l'utilisateur
        String[] answer = question.reponses;
        int nbanswer = 0;

        for (int x = 0; x<length(answer); x++) {
            if (!equals(answer[x], "null")) {
                nbanswer++;
            }
        }

        showHeader();
        centerText("Question " + nbquestion);
        print("\n\n");
        centerText(question.question);
        print("\n\n");
        for (int x = 0; x < nbanswer; x++) {
            centerText((x+1) + ") " + answer[x]);
        }
        print("\n\n");

        int userinput = getinputuser(nbanswer);

        showHeader();
        centerText("Question " + nbquestion);
        print("\n\n");
        centerText(question.question);
        print("\n\n");

        if (userinput == question.bonneReponse) { // Si la réponse est correcte
            for (int x = 0; x < nbanswer; x++) {
                if (x == userinput-1) {
                    text("green");
                    centerText((x+1) + ") " + answer[x]);
                    text("white");
                } else {
                    text("red");
                    centerText((x+1) + ") " + answer[x]);
                    text("white");
                }
            }
            return true;
        } else {
            for (int x = 0; x < nbanswer; x++) { // Si la réponse est incorrecte
                if (x == userinput-1) {
                    text("red");
                    centerText((x+1) + ") " + answer[x]);
                    text("white");
                } else {
                    centerText((x+1) + ") " + answer[x]);
                }
            }
            return false;
        }
    }

    int menuprincipal() { // Affiche le menu principal
        showHeader();
        centerText("Menu Principal");
        print("\n\n"); 
        centerText("1 - Nouvelle partie");
        centerText("2 - Charger partie ");
        centerText("3 - Crédits        ");
        centerText("4 - Quitter jeu    ");
        print("\n\n");
        return getinputuser(4);
    }


    // Fonctions pour question

    Question newQuestion(String question, String[] answer, int goodanswer, int noquestion) { // Créer une nouvelle question
        Question q = new Question();
        q.question = question;
        int y = 0;
        q.reponses = new String[length(answer)];
        for (int x = 0; x < length(answer); x++) {
            if (answer[x] != "") {
                q.reponses[y] = answer[x];
                y++;
            }
        }
        q.bonneReponse = goodanswer;
        q.noquestion = noquestion;
        return q;
    }

    void removeQuestionFromList(Question[] questions, int index) { // Supprime une question d'une liste de questions et décale le reste des questions
        questions[index] = null;
        for (int i = index; i < maxquestions-1; i++) {
            questions[i] = questions[i+1];
        }
    }

    void removeQuestionWithNoQuestionFromList(Question[] questions, int noquestion) {
        for (int i = 0; i < maxquestions; i++) {
            if (questions[i].noquestion == noquestion) {
                removeQuestionFromList(questions, i);
                break;
            }
        }
    }

    void testRemoveQuestionFromList() { // Test de la fonction removeQuestionFromList
        maxquestions = 5;
        Question[] questions = new Question[5];
        questions[0] = newQuestion("Question 1", new String[]{"Réponse 1", "Réponse 2", "Réponse 3", "Réponse 4"}, 1, 1);
        questions[1] = newQuestion("Question 2", new String[]{"Réponse 1", "Réponse 2", "Réponse 3", "Réponse 4"}, 2, 2);
        questions[2] = newQuestion("Question 3", new String[]{"Réponse 1", "Réponse 2", "Réponse 3", "Réponse 4"}, 3, 3);
        questions[3] = newQuestion("Question 4", new String[]{"Réponse 1", "Réponse 2", "Réponse 3", "Réponse 4"}, 4, 4);
        questions[4] = newQuestion("Question 5", new String[]{"Réponse 1", "Réponse 2", "Réponse 3", "Réponse 4"}, 1, 5);
        removeQuestionFromList(questions, 2);
        
        assertEquals(questions[0].noquestion, 1);
        assertEquals(questions[1].noquestion, 2);
        assertEquals(questions[2].noquestion, 4);
        assertEquals(questions[3].noquestion, 5);
    }

    void testRemoveQuestionWithNoQuestionFromList() { // Test de la fonction removeQuestionWithNoQuestionFromList
        maxquestions = 5;
        Question[] questions = new Question[5];
        questions[0] = newQuestion("Question 1", new String[]{"Réponse 1", "Réponse 2", "Réponse 3", "Réponse 4"}, 1, 1);
        questions[1] = newQuestion("Question 2", new String[]{"Réponse 1", "Réponse 2", "Réponse 3", "Réponse 4"}, 2, 2);
        questions[2] = newQuestion("Question 3", new String[]{"Réponse 1", "Réponse 2", "Réponse 3", "Réponse 4"}, 3, 3);
        questions[3] = newQuestion("Question 4", new String[]{"Réponse 1", "Réponse 2", "Réponse 3", "Réponse 4"}, 4, 4);
        questions[4] = newQuestion("Question 5", new String[]{"Réponse 1", "Réponse 2", "Réponse 3", "Réponse 4"}, 1, 5);
        removeQuestionWithNoQuestionFromList(questions, 3);
        
        assertEquals(questions[0].noquestion, 1);
        assertEquals(questions[1].noquestion, 2);
        assertEquals(questions[2].noquestion, 4);
        assertEquals(questions[3].noquestion, 5);
    }

    // Fonctions pour les levels

    Question[] getQuestionsFromLevel(String filename) { // Récupère les questions d'un niveau
        CSVFile file = loadCSV("ressources/levels/" + filename,';');
        int nblines = file.rowCount()-1;
        
        Question[] questions = new Question[nblines];
        for (int i = 1; i < file.rowCount(); i++) {
            questions[i-1] = newQuestion(file.getCell(i, 1), new String[]{file.getCell(i, 2), file.getCell(i, 3), file.getCell(i, 4), file.getCell(i, 5)},stringToInt(file.getCell(i, 6)), stringToInt(file.getCell(i, 0)));
        }

        return questions;
    }

    String getLevelNameFromFile(String filename) { // Récupère le nom du niveau depuis son fichier
        CSVFile file = loadCSV("ressources/levels/" + filename,';');
        return file.getCell(0, 0);
    }

    String getPeriodNameFromFile(String filename) { // Récupère le nom de la période depuis son fichier
        CSVFile file = loadCSV("ressources/levels/" + filename,';');
        return file.getCell(0, 4);
    }

    String[] getParametersFromLevel(String filename) { // Récupère les paramètres du niveau depuis son fichier
        CSVFile file = loadCSV("ressources/levels/" + filename,';');
        String[] parameters = new String[4];
        parameters[0] = file.getCell(0, 1);
        parameters[1] = file.getCell(0, 2);
        parameters[2] = file.getCell(0, 3);
        parameters[3] = file.getCell(0, 4);
        return parameters;
    }

    boolean rightLevel(String filename, int mode) { // Vérifie auprès de l'utilisateur si le niveau sélectionné est le bon
        String[] param = getParametersFromLevel(filename);
        int nbquestion = length(getQuestionsFromLevel(filename));
        showHeader();
        centerText(getLevelNameFromFile(filename));
        print("\n");
        String periodName = getPeriodNameFromFile(filename);
        if (mode == 1) {
            centerText(param[0] +" questions par " + periodName);
            centerText(param[1] +" " + periodName + "s");
        } else if (mode == 2 || mode == 4) { // Mode survie & Mode Mort Subite
            centerText(nbquestion + " questions ");
        } else if (mode == 3) { // Mode Burger de la Mort
            centerText("10 questions d'un coup");
        }
        print("\n\n");
        centerText("Voulez-vous jouer à ce niveau ?");
        print("\n");
        centerText("1. Oui");
        centerText("2. Non");
        print("\n\n");
        println();
        int input = getinputuser(2);
        if (input == 1) {
            return true;
        } else {
            return false;
        }
    }

    // Fonctions pour les sauvegardes

    boolean rightSave(String filename) { // Vérifie auprès de l'utilisateur si la sauvegarde sélectionnée est la bonne
        String[] param = getParametersFromSave(filename);

        showHeader();
        centerText(getSaveNameFromFile(filename));
        centerText(getPeriodNameFromFile(param[0]) +" "+ param[1]);
        print("\n\n");
        centerText("Voulez-vous charger cette sauvegarde ?");
        print("\n");
        centerText("1. Oui");
        centerText("2. Non");
        print("\n\n");

        int input = getinputuser(2);
        if (input == 1) {
            return true;
        }
        return false;
    }

    String getSaveNameFromFile(String filename) { // Récupère le nom de la sauvegarde depuis son fichier
        CSVFile file = loadCSV("ressources/saves/" + filename,';');
        return file.getCell(0, 0);
    }

    String[] getParametersFromSave(String filename) { // Récupère les paramètres de la sauvegarde depuis son fichier
        CSVFile file = loadCSV("ressources/saves/" + filename,';');
        String[] parameters = new String[2];
        parameters[0] = file.getCell(0, 1);
        parameters[1] = file.getCell(0, 2);
        return parameters;
    }

    boolean interSemester(int actualsemester) { // Affiche le menu entre deux périodes
        print(ANSI_CLEAR_SCREEN_ALL);
        showTextFromCSV("congrat");
        print("\n\n\n");
        centerText("Vous avez réussi votre " + periodname + " " + actualsemester + " !");
        print("\n\n");
        centerText("Que voulez-vous faire ?");
        print("\n\n");
        centerText("1. Continuer  ");
        centerText("2. Sauvegarder");
        centerText("3. Quitter    ");
        print("\n\n");
        int input = getinputuser(3);
        if (input == 1) {
            return true;
        } else if (input == 2) {
            createSave(actualsemester);
            return interSemester(actualsemester);
        }
        return false;
    }

    void createSave(int actualsemester) { // Créer une sauvegarde
        String[][] save = new String[2][length(removedquestions)];
        String savename = "";
        do {
            print("Quelle est le nom de la sauvegarde ? ");
            savename = readString();
        } while (savename == "" || savename == "return");
        if (fileExist("ressources/saves/" + savename + ".csv")) {
            print(ANSI_CLEAR_SCREEN_ALL);
            centerText("Une sauvegarde avec ce nom existe déjà !");
            centerText("Voulez-vous la remplacer ?");
            centerText("1. Oui");
            centerText("2. Non");
            int input = getinputuser(2);
            if (input == 1) {
                save[0][0] = savename;
                save[0][1] = nameLevel;
                save[0][2] = "" + actualsemester;
                for (int i = 0; i < nbremovedquestions; i++) {
                    save[1][i] = "" + removedquestions[i];
                }
                saveCSV(save,"ressources/saves/" + savename + ".csv", ';');
                println("La sauvegarde a été créée avec succès !");
                waitinputuser();
            } else {
                print(ANSI_CLEAR_SCREEN_ALL);
                centerText("La sauvegarde n'a pas été créée !");
                waitinputuser();
            }
        } else {
            save[0][0] = savename;
            save[0][1] = nameLevel;
            save[0][2] = "" + actualsemester;
            for (int i = 0; i < nbremovedquestions; i++) {
                save[1][i] = "" + removedquestions[i];
            }
            saveCSV(save,"ressources/saves/" + savename + ".csv", ';');
            println("La sauvegarde a été créée avec succès !");
            waitinputuser();
        }
        
    }

    // Fonctions pour les modes de jeu
    /// Mode Normal

    void runNormalModeLevel(Question[] questions, int[] parameters, int actualperiod) { // Lance un niveau
        int nbquestion = parameters[0];
        int nbperiod = parameters[1];
        int questionfail = parameters[2];
        int nbperiodpassed = actualperiod;
        boolean end = false;
        while (nbperiodpassed < nbperiod && maxquestions > 0) { 
            int success = runPeriode(questions, nbquestion, questionfail, nbperiodpassed+1);
            if (success == 1) { // Si l'utilisateur a réussi la période
                nbperiodpassed++;
            } else if (success == 0) { // Si l'utilisateur a échoué la période
                println("Vous avez échoué votre "+ periodname +"...");
                waitinputuser();
                break;
            } else if (success == 2) { // Si il n'y a plus de questions
                println("Il n'y plus de questions...");
                waitinputuser();
                break;
            }
            if ((parameters[2]+1) != nbperiodpassed && !interSemester(nbperiodpassed)) { // Si l'utilisateur ne veut pas continuer
                end = true;
                algorithm();
                break;
            }
        }
        if (!end) {
            if (nbperiodpassed == nbperiod || maxquestions == 0) {
                println("Vous avez gagné");
            } else {
                println("Vous avez perdu");
            }
        }
        algorithm();
    }

    int runPeriode(Question[] questions, int nbquestion, int nbquestionfail, int nbperiod) { // Lance une période
        int questionfailed = 0;
        int index;
        Question actualquestion;

        int x = 0;
        
        while (x < nbquestion && questionfailed < nbquestionfail && maxquestions > 0) {

            /*for (int y = 0; y < maxquestions; y++) { //Pour Debug
                print((y+1) + " - ");
                print(questions[y].question + " - ");
                print(questions[y].reponses[0] + " - ");
                print(questions[y].reponses[1] + " - ");
                print(questions[y].reponses[2] + " - ");
                print(questions[y].reponses[3] + " - ");
                println(questions[y].bonneReponse);
            }*/

            index = (int) (random()*maxquestions);
            actualquestion = questions[index];
            if (askquestion(actualquestion, x+1, nbperiod)) {
                println("Bravo ! Vous avez répondu correctement à la question !\n");
                removeQuestionFromList(questions, index);
                removedquestions[nbremovedquestions] = actualquestion.noquestion;
                maxquestions--;
                nbremovedquestions++;
            } else {
                println("Mince ! Votre réponse est incorrecte...\n");
                questionfailed++;
            }
            x++;
            waitinputuser();
        }

        if (length(questions) == 0) {
            return 2;
        } else if (nbquestionfail <= questionfailed) {
            return 0;
        } else {
            return 1;
        } 
    }

    /// Mode Survie

    void runSurvieModeLevel(Question[] questions) {
        int nbquestion = length(questions);
        boolean end = false;
        int index;
        Question actualquestion;
        int questionpassed = 0;
        int hp = 3;
        int maxhp = hp;
        while (!end && hp > 0 && questionpassed < nbquestion) {
            index = (int) (random()*(nbquestion-questionpassed));
            actualquestion = questions[index];
            if(askquestion(actualquestion, questionpassed+1,hp,maxhp)) {
                println("Bravo ! Vous avez répondu correctement à la question !");
                removeQuestionFromList(questions, index);
            } else {
                println("Mince ! Votre réponse est incorrecte...");
                hp--;
            }
            if (hp < 1) {
                end = true;
            }
            questionpassed++;
            waitinputuser();
        }
        if (!end) {
            println("Il n'y a plus de question, vous avez répondu correctement à "+ questionpassed +" questions !");
        } else {
            println("Vous avez plus de HP, vous avez perdu...\nVous avez répondu correctement à " + questionpassed + " questions !");
        }
        algorithm();
    }

    /// Mode Mort Subite

    void runMortSubiteModeLevel(Question[] questions) { // Lance un niveau
        int nbquestion = length(questions);
        boolean end = false;
        int nbquestionpassed = 0;
        int index;
        Question actualquestion;
        while (nbquestionpassed < nbquestion && !end) { 
            index = (int) (random()*(nbquestion-nbquestionpassed));
            actualquestion = questions[index];
            if(askquestion(actualquestion, nbquestionpassed+1)) {
                println("Bravo ! Vous avez répondu correctement à la question !\n");
                removeQuestionFromList(questions, index);
                nbquestionpassed++;
            } else {
                println("Mince ! Votre réponse est incorrecte...\n");
                end = true;
            }
            waitinputuser();
        }
        if (!end) {
            println("Il n'y a plus de question, vous avez répondu correctement à "+ nbquestionpassed +" questions !");
        } else {
            println("Vous avez perdu...\n Vous avez répondu correctement à " + nbquestionpassed + " questions sur " + nbquestion + " !");
        }
        algorithm();
    }

    /// Mode Burger de la Mort

    void runBurgerModeLevel(Question[] questions) {
        int nbquestion = length(questions);
        boolean end = false;
        int index;
        Question actualquestion;
        Question[] questionSelected = new Question[10];
        int[] nbanswerquestions = new int[10];
        int nbquestionpassed = 0;

        for (int x = 0; x<10; x++) { // Boucle pour l'affichage des 10 questions
            index = (int) (random()*(nbquestion-x));
            actualquestion = questions[index];
            questionSelected[x] = actualquestion;
            removeQuestionFromList(questions, index);

            String[] answer = actualquestion.reponses;
            int nbanswer = 0;
            for (int y = 0; y<length(answer); y++) {
                if (!equals(answer[y], "null")) {
                    nbanswer++;
                }
            }
            nbanswerquestions[x] = nbanswer;

            showquestion(actualquestion, x+1);
            waitinputuser();
        }

        for (int x = 0; x<10; x++) { // Boucle pour les réponses aux 10 questions
            showHeader();
            centerText("Question " + (x+1));
            int input = getinputuser(nbanswerquestions[x]);
            if (input == questionSelected[x].bonneReponse) { // Si la réponse est correcte
                showHeader();
                centerText("Question " + (x+1));
                centerText(questionSelected[x].question);
                print("\n");

                for (int y = 0; y < nbanswerquestions[x]; y++) {
                    if (y == input-1) {
                        text("green");
                        centerText((y+1) + ") " + questionSelected[x].reponses[y]);
                        text("white");
                    } else {
                        text("red");
                        centerText((y+1) + ") " + questionSelected[x].reponses[y]);
                        text("white");
                    }
                }
                print("\n\n");
                nbquestionpassed++;
                println("Bravo ! Vous avez répondu correctement à la question !\n");
                waitinputuser();
            } else { // Si la réponse est incorrecte
                showHeader();
                centerText("Question " + (x+1));
                centerText(questionSelected[x].question);
                print("\n");

                for (int y = 0; y < nbanswerquestions[x]; y++) {
                    if (y == input-1) {
                        text("red");
                        centerText((y+1) + ") " + questionSelected[x].reponses[y]);
                        text("white");
                    } else {
                        centerText((y+1) + ") " + questionSelected[x].reponses[y]);
                    }
                }
                print("\n\n");
                println("Mince ! Votre réponse est incorrecte...\n");
                waitinputuser();
                end = true;
                break;
            }
        }
        if (!end) {
            println("Vous avez gagné !");
        } else {
            println("Vous avez réussi à répondre correctement à " + nbquestionpassed + " questions sur 10 !");
        }
        algorithm();
    }
}