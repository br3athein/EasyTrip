package com.example.br3athe_in.easyTrip.Util;

import java.util.ArrayList;
import java.util.Random;

public final class Abc {
	// ну, он же вроде как static.
	private Abc () {}
	// region Прекомпилируемая настройка алгоритма

	// Макс. количество итераций, после которого локальный поиск завершается.
	private static final int MAX_ITERATIONS_IN_STAGNATION = 100;
	// Макс. количество итераций, после которого пчёлы,
	// не получая прибавки в результате, прекращают поиск.
	private static final int MAX_ITERATIONS_LOCAL = 1000;
	// Макс. количество итераций, после которого уже весь алгоритм останавливает работу.
	private static final int MAX_ITERATIONS_GLOBAL = 100;

	// Количество фуражиров в рою (фактически, количество решений, получаемых локальным поиском)
	private static final int FORAGERS_QUANTITY = 20;
	// Количество разведчиков (рабочие, которые занимаются случайным поиском)
	private static final int SCOUT_QUANTITY = 5;

	// endregion
	// region Исходные данные плюс метод вызова алгоритма

	// Входные данные

	/** Набор выбраных городов */

	private static Random r = new Random();

	/**
	 * На вызове <code>optimize</code> алгоритм отрабатывает полностью, если чо.
	 */
	public static ArrayList<City> optimize(ArrayList<City> cities)
	{
		// Банальная арифметическая прогрессия: 0 - cities.size().
		//Она же - отныне и вовек - нумерация исходных городов.
		ArrayList<Integer> initialSequenceRenumerated = new ArrayList<>(cities.size());
		for (int i = 0; i < cities.size(); i++) {
			initialSequenceRenumerated.add(i);
		}

		// Точно то же самое, но для оптимального порядка.
		ArrayList<Integer> optimalSequenceRenumerated;

		// "Поехали!" © Гагарин?..
		optimalSequenceRenumerated = singleRun(initialSequenceRenumerated);

		// Ну, покатались - и хватит.
		// Теперь толкуем кучу непонятных цифр, транслируем в не менее адекватный ответ.
		ArrayList<City> optimalRoute = new ArrayList<>();
		for (int i : optimalSequenceRenumerated) {
			optimalRoute.add(cities.get(optimalSequenceRenumerated.get(i)));
		}
		return optimalRoute;
	}
	// endregion
	// region Имплементация globalRun и смежных методов
	// Тут мог быть ваш globalRun. Как жаль, что он нахуй не нужен для частной задачи коммивояжёра. :3
	// Нет, правда, очень жаль.
	// R.I.P., дорогие 86 строк.
	// endregion
	// region Имплементация singleRun и смежных методов

	private static ArrayList<Integer> singleRun(ArrayList<Integer> initialSequence) {
		try {
			int stagnatingFor = 0,
					singleRunIterationCounter = 0,
					previousRating = 0,
					currentRating;

			// ПУНКТ 1. //
			// Начальная (!) выборка в INTEL_SCOUT_QUANTITY участков
			// (а также RANDOM_SCOUT_QUANTITY шт. на случайный поиск).
			ArrayList<ArrayList<Integer>> colonyTerritory
							= new ArrayList<>(FORAGERS_QUANTITY + SCOUT_QUANTITY);

			colonyTerritory.add(initialSequence); // # Bug inside © - think not

			for (int i = 1; i < FORAGERS_QUANTITY + SCOUT_QUANTITY; i++) {
				insertPathInOrder(colonyTerritory, rearrange(initialSequence));
			}

			// ПУНКТ 2. //
			// Сортировка "участков" в "территории" в соответствии с их качеством.

			// Начало оптимизационного цикла... //
			do {
				// Оценка наилучшего результата.
				currentRating = routeRating(colonyTerritory.get(0));

				// ПУНКТ 3. //
				// Отбор трёх лучших для поиска в окрестностях
				ArrayList<ArrayList<Integer>> bestSequences =
								new ArrayList<>(colonyTerritory.subList(0, 2));

				// ПУНКТ 4. //
				// Закрепление фуражиров за злачными местами

				// Количество фуражиров на каждом из злачных мест.
				int[] deployedForagersQuantity = new int[3];

				for (int i = 0; i < 3; i++) {
					deployedForagersQuantity[i] = sequenceSatisfaction(colonyTerritory.get(i));
				}

				// Если три наилучших - недопустимы:
				// распределить фуражиров равномерно между тремя наилучшими
				if (sum(deployedForagersQuantity) == 0) {
					deployedForagersQuantity[0] =
									deployedForagersQuantity[1] =
													deployedForagersQuantity[2] =
																	// ACHTUNG! Деление.
																	FORAGERS_QUANTITY / 3;
				} else {
					// иначе - как обычно, пропорционально их ЦФ
					// ACHTUNG! Деление.
					double ForagerProportionValue = Double.valueOf(FORAGERS_QUANTITY)
									/ Double.valueOf(sum(deployedForagersQuantity));

					// Модуляция количества фуражиров под их максимальное количество
					for (int i = 0; i < 3; i++) {
						deployedForagersQuantity[i] = (int) Math.round(
										Double.valueOf(deployedForagersQuantity[i]) * ForagerProportionValue);
					}
				}

				// Полагаю, что частенько из-за деления целых чисел при модуляции
				// есть реальная возможность одного фуражира потерять в пользу всяких там округлений.
				// Поэтому самым невезучим накидывается один бонусный в случае выявления пропажи.
				// И, да, я свято верю в то, что значение sum(deployedForagersQuantity)
				// не переплюнет по модулю Integer.maxValue.
				if (sum(deployedForagersQuantity) == FORAGERS_QUANTITY + 2) {
					deployedForagersQuantity[1]--;
					deployedForagersQuantity[2]--;
				} else if (sum(deployedForagersQuantity) == FORAGERS_QUANTITY + 1) {
					deployedForagersQuantity[2]--;
				} else if (sum(deployedForagersQuantity) == FORAGERS_QUANTITY - 1) {
					deployedForagersQuantity[2]++;
				} else if (sum(deployedForagersQuantity) == FORAGERS_QUANTITY - 2) {
					deployedForagersQuantity[1]++;
					deployedForagersQuantity[2]++;
				}

				// ПУНКТ 5. //
				// Локальный поиск в злачных местах
				colonyTerritory.clear();
				for (ArrayList<Integer> Area : bestSequences) {
					colonyTerritory.add(Area);
				}

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < deployedForagersQuantity[i] - 1; j++) {
						insertPathInOrder(colonyTerritory, localSearch(bestSequences.get(i)));
					}
				}

				// ПУНКТ 6. //
				// Отправка разведчиков
				// Генерация новых, случайных решений
				for (int i = 0; i < SCOUT_QUANTITY; i++) {
					insertPathInOrder(colonyTerritory, rearrange(initialSequence));
				}

				// ПУНКТ 7. //
				// Проверка на стагнацию.
				if (currentRating != previousRating) {
					stagnatingFor = 0;
					previousRating = currentRating;
				} else {
					stagnatingFor++;
				}

				singleRunIterationCounter++;
				// Если после MAX_ITERATIONS_IN_STAGNATION итераций решение не желает меняться
				// ИЛИ ЖЕ алгоритм вконец разбушевался и отработал уже целых
				// MAX_ITERATIONS_GLOBAL итераций:
				// выход из алгоритма, возвращение наилучшего пути в случае длительной стагнации.
			} while (stagnatingFor < MAX_ITERATIONS_IN_STAGNATION
							&& singleRunIterationCounter < MAX_ITERATIONS_GLOBAL);

			return colonyTerritory.get(0);
		} catch (ArithmeticException e) {
			// Вероятность критической ситуации аннулирована, catch до сих пор висит.
			// Хотя практика всё же показывает, что экстерминатус таки имеет место быть.
			// Хотя нет, уже не имеет.
			// Короче, вылетал, если топ3 - недопустимы.
			// Дело закрыто.
			return null;
		}
	}

	private static int sum(int[] incoming) {
		int sum = 0;
		for(int i : incoming) {
			sum += i;
		}
		return sum;
	}

	/**
	 * Вставка нового участка в территорию при соблюдении спадающего
	 * порядка по ценности (длине пути).
	 * Т.е. первым стоит наилучший участок.
	 */
	private static void insertPathInOrder(ArrayList<ArrayList<Integer>> destination, ArrayList<Integer> value) {
		int insertionIndex = 0;

		if (destination.size() == 0) {
			destination.add(value);
		} else {
			double valueTiming = routeRating(value);

			while (insertionIndex < destination.size() && routeRating(destination.get(insertionIndex)) < valueTiming) {
				insertionIndex++;
			}

			destination.add(insertionIndex, value);
		}
	}

	/**
	 * @param initialSequence Выборка из множества городов
	 * @return Рандомная перестановка этой выборки
	 */
	private static ArrayList<Integer> rearrange(ArrayList<Integer> initialSequence) {
		int notSoRandomCity;
		ArrayList<Integer> randomizedSequence = new ArrayList<>(initialSequence.size());
		ArrayList<Integer> initialSequenceCopy = new ArrayList<>(initialSequence);

		while (initialSequenceCopy.size() > 0) {
			notSoRandomCity = r.nextInt(initialSequenceCopy.size());
			randomizedSequence.add(initialSequenceCopy.get(notSoRandomCity));
			initialSequenceCopy.remove(notSoRandomCity);
		}

		return randomizedSequence;
	}

	/**
	 * Хождение вокруг да около, короче
	 */
	private static ArrayList<Integer> localSearch(ArrayList<Integer> initialSequence) {
		int iterationCounter = 0;
		ArrayList<Integer> searchResult;

		do {
			searchResult = hammingStray(initialSequence);
			iterationCounter++;
		} while (sequenceSatisfaction(initialSequence) < sequenceSatisfaction(searchResult)
						|| iterationCounter < MAX_ITERATIONS_LOCAL);

		return searchResult;
	}

	// endregion
	// region Общий технический инструментарий

	/**
	 * Расчёт целевой для данного пути (эстетическая ценность, максимизация)
	 * (если учитывать только эстетику, получится либо константа, либо нуль.
	 * Так себе перспектива, выхлоп инфы - минимум.)
	 */
	private static int sequenceSatisfaction(ArrayList<Integer> Sequence) {
		int totalSatisfaction = 0; // шо за ересь, фи.

		for(int cc : Sequence);
			//totalSatisfaction += estetics.get(cc);

		return totalSatisfaction;
	}

	/*
	 * Длина пути в КИЛОМЕТРАХ БЛЯТЬ, наконец-то.
	 */
	private static int routeRating(ArrayList<Integer> route) {
		if (route.size() == 0) {
			return 0;
		}

		int totalDist = 0;

		for (int i = 0; i < route.size() - 1; i++) {
			// ну ты понел
			//totalDist += wayLength.get(route.get(i)).get(route.get(i + 1));
		}
		//totalDist += citiesVisitTime.get(route.get(route.size() - 1));

		return totalDist;
	}

	/**
	 * Расстояние между двумя генами по Хеммингу.
	 */
	private int hammingDistance(ArrayList<Integer> a, ArrayList<Integer> b) {
		int actualDistance = 0;

		if (a.size() != b.size()) return -1;

		for (int i = 0; i < a.size(); i++) {
			if (a.get(i).equals(b.get(i))) {
				actualDistance++;
			}
		}
		return actualDistance;
	}

	/**
	 * Мутации, они же отстранения по Хеммингу.
	 */
	private static ArrayList<Integer> hammingStray(ArrayList<Integer> initialSequence) {
		int strayRadius = 0;
		ArrayList<Integer> strayedSequence = new ArrayList<>(initialSequence);

		// Квази-вероятность следующей мутации - (1/3)^(кол-во уже совершённых мутаций).
		// Для strayRadius = 0 тоже справедливо, чо.
		do {
			swap(r.nextInt(strayedSequence.size()), r.nextInt(strayedSequence.size()), strayedSequence);
			strayRadius++;
		}
		while (r.nextInt(3 ^ strayRadius) == 0);

		return strayedSequence;
	}

	/**
	 * Шо, первый раз замужем?
	 */
	private static void swap(int p1, int p2, ArrayList<Integer> sequence) {
		try {
			int Buffer = sequence.get(p1);
			sequence.set(p1, sequence.get(p2));
			sequence.set(p2, Buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// endregion
}
