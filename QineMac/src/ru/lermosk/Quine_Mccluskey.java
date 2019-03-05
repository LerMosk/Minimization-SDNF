package ru.lermosk;


import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс, осуществляющий минимизацию функции, заданной СДНФ
 */
public class Quine_Mccluskey {

    private Quine_Mccluskey() {
    }

    /**
     * Осуществляет минимизацию функции, заданной СДНФ.
     * @param miniterms - список значений переменных, на которых функция принимает занчение "1"
     * @return - минимизированную функцию
     */
    public static String minimization(List<String> miniterms) {
        List<String> primaryImplicants = findPrimaryImplicants(miniterms);
        HashMap<Pair<String, String>, Boolean> table = tagging(miniterms, primaryImplicants);
        List<String> significantImplicants = findSignificantImplicants(table, miniterms, primaryImplicants);
        List<String> resultImplicants = deletePrimaryImplicants(table, miniterms, primaryImplicants, significantImplicants);
        List<String> minCoverage = findMinimumCoverage(table, miniterms, resultImplicants);
        return decode(minCoverage);
    }

    /**
     * Поиск первичных импликант.
     * @param miniterms - список значений переменных, на которых функция принимает занчение "1" (минитермы)
     * @return - список первичных импликант
     */
    private static List<String> findPrimaryImplicants(List<String> miniterms) {

        List<String> primaryImplicants = new ArrayList<>();
        HashMap<Integer, ArrayList<String>> implicantsByWeight = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            implicantsByWeight.put(i, new ArrayList<>());
        }
        for (String str : miniterms) {
            implicantsByWeight.get(getWeight(str, '1')).add(str);
        }


        for (int f = 0; f < 5; f++) {
            HashMap<String, Boolean> flags = new HashMap<>();
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < implicantsByWeight.get(i).size(); j++) {
                    flags.put(implicantsByWeight.get(i).get(j), false);
                }
            }
            HashMap<Integer, ArrayList<String>> implicantsByWeightTemp = new HashMap<>();
            for (int i = 0; i < 6; i++) {
                implicantsByWeightTemp.put(i, new ArrayList<>());
            }
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < implicantsByWeight.get(i).size(); j++) {
                    for (int k = 0; k < implicantsByWeight.get(i + 1).size(); k++) {
                        String compareResult = compareImplicants(implicantsByWeight.get(i).get(j), implicantsByWeight.get(i + 1).get(k));
                        if (compareResult.compareTo("notMatches") != 0) {
                            flags.put(implicantsByWeight.get(i).get(j), true);
                            flags.put(implicantsByWeight.get(i + 1).get(k), true);
                            implicantsByWeightTemp.get(getWeight(compareResult, '1')).add(compareResult);
                        }
                    }
                }
            }
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < implicantsByWeight.get(i).size(); j++) {
                    if (flags.get(implicantsByWeight.get(i).get(j)) == false)
                        if (!primaryImplicants.contains(implicantsByWeight.get(i).get(j)))
                            primaryImplicants.add(implicantsByWeight.get(i).get(j));
                }
            }
            implicantsByWeight = implicantsByWeightTemp;
        }
        return primaryImplicants;
    }

    /**
     * Подсчитывает количество символов в строке
     * @param ch -подсчитываемые символы
     * @param str -строка
     * @return - количество символов
     */
    private static int getWeight(String str, char ch) {
        int weight = 0;
        for (char element : str.toCharArray()) {
            if (element == ch) weight++;
        }
        return weight;
    }

    /**
     * Сравнивает 2 минитерма.
     * @param impl1 - 1-ый минитерм для сравнения
     * @param impl2 - 2-ой минитерм для сравнения
     * @return - минитерм ранга на 1 меньше или "notMatches", если не удалось получить новый минитерм
     */
    private static String compareImplicants(String impl1, String impl2) {
        String result = "";
        boolean oneSymbolNotMatches = false;
        for (int i = 0; i < impl1.length(); i++) {
            if (impl1.charAt(i) != impl2.charAt(i)) {
                if (oneSymbolNotMatches == false) {
                    oneSymbolNotMatches = true;
                    result += ".";
                } else return "notMatches";
            } else {
                result += impl1.charAt(i);
            }
        }
        return result;
    }

    /**
     * Расстановка меток.
     * @param miniterms - список минитерм
     * @param primaryImplicants - список первичных импликант
     * @return - поле с метками(таблица)
     */
    private static HashMap<Pair<String, String>, Boolean> tagging(List<String> miniterms, List<String> primaryImplicants) {
        HashMap<Pair<String, String>, Boolean> table = new HashMap<>();
        for (String primImpl : primaryImplicants) {
            for (String impl : miniterms) {
                if (impl.matches(primImpl) == true) {
                    table.put(new Pair<>(primImpl, impl), true);
                } else table.put(new Pair<>(primImpl, impl), false);
            }
        }
        return table;
    }

    /**
     * Нахождение существенных импликант.
     * @param table - поле с метками
     * @param miniterms - список минитерм
     * @param primaryImplicants - список первичных импликант
     * @return - список существенных импликант
     */
    private static List<String> findSignificantImplicants(HashMap<Pair<String, String>, Boolean> table, List<String> miniterms, List<String> primaryImplicants) {
        List<String> significantImplicants = new ArrayList<>();
        for (String impl : miniterms) {
            boolean onePlus = false;
            String signImpl = "";
            for (String primImpl : primaryImplicants) {
                if (table.get(new Pair<>(primImpl, impl)) == true) {
                    if (onePlus == false) {
                        onePlus = true;
                        signImpl = primImpl;
                    } else {
                        onePlus = false;
                        break;
                    }
                }

            }
            if (onePlus == true)
                if (!significantImplicants.contains(signImpl))
                    significantImplicants.add(signImpl);
        }
        return significantImplicants;
    }

    /**
     * Удаление лишних первичных импликант.
     * @param table - поле с метками
     * @param miniterms - список минитерм
     * @param primaryImplicants - список первичных импликант
     * @param significantImplicants - список существенных импликант
     * @return - список импликант без лишних првичных импликант
     */
    private static List<String> deletePrimaryImplicants(HashMap<Pair<String, String>, Boolean> table, List<String> miniterms, List<String> primaryImplicants, List<String> significantImplicants) {
        List<String> resultImplicants = new ArrayList<>();
        resultImplicants.addAll(significantImplicants);
        List<String> coveredImplicants = new ArrayList<>();
        for (String signImpl : significantImplicants) {
            for (String impl : miniterms) {
                if (table.get(new Pair<>(signImpl, impl)) == true) {
                    if (!coveredImplicants.contains(impl)) {
                        coveredImplicants.add(impl);
                    }
                }
            }
        }
        for (String primImpl : primaryImplicants) {
            for (String impl : miniterms) {
                if (!resultImplicants.contains(primImpl) && table.get(new Pair<>(primImpl, impl)) == true && !coveredImplicants.contains(impl)) {
                    resultImplicants.add(primImpl);
                }
            }
        }
        return resultImplicants;
    }

    /**
     * Поиск минимального покрытия
     * @param table - поле с метками
     * @param miniterms - список минитерм
     * @param resultImplicants - список первичных импликант без удалённых на предыдущем шаге
     * @return - список импликант, образующих минимальное покрытие
     */
    private static List<String> findMinimumCoverage(HashMap<Pair<String, String>, Boolean> table, List<String> miniterms, List<String> resultImplicants) {
        List<String> minimumCoverage = new ArrayList<>();
        List<String> coveredImplicants = new ArrayList<>();
        HashMap<Integer, ArrayList<String>> implicantsByWeight = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            implicantsByWeight.put(i, new ArrayList<>());
        }
        for (String impl : resultImplicants) {
            implicantsByWeight.get(getWeight(impl, '.')).add(impl);
        }
        for (int i = 2; i > 0; i--) {
            List<Pair<Integer, Integer>> implicantsOrder = implicantOrder(table, miniterms, implicantsByWeight.get(i), coveredImplicants);
            for (int j = 0; j < implicantsOrder.size(); j++) {
                String minImpl = implicantsByWeight.get(i).get(implicantsOrder.get(j).getValue());
                for (String impl : miniterms) {
                    if (table.get(new Pair<>(minImpl, impl)) == true && !coveredImplicants.contains(impl)) {
                        coveredImplicants.add(impl);
                        if (!minimumCoverage.contains(minImpl)) {
                            minimumCoverage.add(minImpl);
                        }
                    }
                }
            }
        }

        return minimumCoverage;
    }

    /**
     * Сортировка импликант.
     * @param table - поле с метками
     * @param miniterms - список минитерм
     * @param implicantsToOrder - список импликант, которые необходимо отсортировать по количеству минитерм,
     *                          которые покрывает этот импликант (по количеству отметок в строке)
     * @param coveredMiniterms - уже покрытые минитермы
     * @return - отсортированный список импликант
     */
    private static List<Pair<Integer, Integer>> implicantOrder(HashMap<Pair<String, String>, Boolean> table, List<String> miniterms, List<String> implicantsToOrder, List<String> coveredMiniterms) {
        List<Pair<Integer, Integer>> implOrder = new LinkedList<>();
        for (int i = 0; i < implicantsToOrder.size(); i++) {
            Integer countTrue = 0;
            for (String impl : miniterms) {
                if (table.get(new Pair<>(implicantsToOrder.get(i), impl)) == true && !coveredMiniterms.contains(impl))
                    countTrue++;
            }

            if (implOrder.isEmpty()) {
                implOrder.add(new Pair<>(countTrue, i));
            } else {
                for (int j = 0; j < implOrder.size(); j++) {
                    if (implOrder.get(j).getKey() < countTrue) {
                        implOrder.add(j, new Pair<>(countTrue, i));
                        break;
                    }
                    if (j == implOrder.size() - 1) {
                        implOrder.add(new Pair<>(countTrue, i));
                        break;
                    }
                }
            }
        }
        return implOrder;
    }

    /**
     * Нахождение существенных импликант.
     * @param result - список итоговых импликант, образующих минимальное покрытие
     * @return - функция, представленная в МДНФ
     */
    private static String decode(List<String> result) {
        String answer = "";
        for (String str : result) {
            for (int i = 5; i > -1; i--) {
                if (str.charAt(5 - i) == '1') {
                    answer += "x" + i;
                } else if (str.charAt(5 - i) == '0') answer += "-x" + i;
            }
            answer += "+";
        }
        return answer.substring(0, answer.length() - 1);
    }

    /**
     * Применение метода Квайна-МакКласски.
     * Из файла достаёт значения, на которых функция принимает значения "1", преобразует их в двоичный вид,
     * подаёт на вход методу класса.
     *
     * Имя файла прописывается первым аргументом командной строки.
     */
    public static void main(String[] args) {
        try {
            String content = new Scanner(new File(args[0])).useDelimiter("\\Z").next();
            List<String> implicants = new ArrayList<>();
            Pattern pattern = Pattern.compile("([\\d][\\d]*),");
            Matcher m = pattern.matcher(content);
            while (m.find()) {
                implicants.add(Integer.toBinaryString(Integer.parseInt(m.group(1))));
            }
            for (int i = 0; i < implicants.size(); i++) {
                String zero = "00000";
                implicants.set(i, zero.substring(0, 6 - implicants.get(i).length()) + implicants.get(i));
            }
            System.out.println(Quine_Mccluskey.minimization(implicants));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
