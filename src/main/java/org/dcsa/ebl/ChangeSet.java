package org.dcsa.ebl;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.UpdateException;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public final class ChangeSet<T> {

    public final List<T> newInstances;
    public final List<T> updatedInstances;
    public final List<T> orphanedInstances;

    public static <T> ChangeSet<T> of(List<T> newInstances, List<T> updatedInstances, List<T> orphanedInstances) {
        return new ChangeSet<>(
                Collections.unmodifiableList(newInstances),
                Collections.unmodifiableList(updatedInstances),
                Collections.unmodifiableList(orphanedInstances)
        );
    }

    public static <T, I> ChangeSet<T> changeListDetector(List<T> listFromOriginal, List<T> listFromUpdate, Function<T, I> idMapper, Consumer<T> validator) {
        return changeListDetector(listFromOriginal, listFromUpdate, idMapper, validator, false);
    }

    public static <T, I> ChangeSet<T> changeListDetector(List<T> listFromOriginal, List<T> listFromUpdate, Function<T, I> idMapper, Consumer<T> validator, boolean allowNewIDs) {
        Map<I, T> knownIds = listFromOriginal.stream().collect(Collectors.toMap(idMapper, Function.identity()));
        Set<I> usedIds = new HashSet<>(listFromUpdate.size());
        List<T> newObjects = new ArrayList<>();
        List<T> updatedObjects = new ArrayList<>(listFromUpdate.size());

        for (T update : listFromUpdate) {
            I updateId = idMapper.apply(update);
            if (updateId != null) {
                if (!knownIds.containsKey(updateId) && !allowNewIDs) {
                    throw new UpdateException("Invalid id: " + updateId
                            + ":  The id is not among the original list of ids (null the ID field if you want to"
                            + " create a new instance)");
                }
                if (!usedIds.add(updateId)) {
                    throw new UpdateException("Duplicate id: " + updateId
                            + ": The entity ID was used more than once in a list where it was expected to be used"
                            + " at most once.");
                }
                updatedObjects.add(update);
            } else {
                newObjects.add(update);
            }
            validator.accept(update);
        }

        return ChangeSet.of(
                newObjects,
                updatedObjects,
                knownIds.entrySet().stream()
                        .filter(entry -> !usedIds.contains(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList())
        );
    }

    public static <OM, IM, ID> ChangeSet<IM> flatteningChangeDetector(List<OM> itemsFromOriginal,
                                                                      List<OM> itemsFromUpdate,
                                                                      Function<OM, List<IM>> mapper,
                                                                      Function<IM, ID> idMapper,
                                                                      String name) {
        Function<OM, Stream<IM>> toIMStream = om -> {
            List<IM> imList = mapper.apply(om);
            if (imList == null || imList.isEmpty()) {
                return Stream.empty();
            }
            return imList.stream();
        };
        Map<ID, IM> knownIM = itemsFromOriginal.stream()
                .flatMap(toIMStream)
                .collect(Collectors.toMap(idMapper, Function.identity()));
        Set<ID> usedIds = new HashSet<>(knownIM.size());
        List<IM> updatedItems = new ArrayList<>(knownIM.size());
        List<IM> newItems = new ArrayList<>();
        itemsFromUpdate.stream()
                .flatMap(toIMStream)
                .forEach(im -> {
                    ID id = idMapper.apply(im);
                    if (knownIM.containsKey(id)) {
                        if (!usedIds.add(id)) {
                            throw new UpdateException(name + " ID " + id
                                    + " is used twice! IDs must be used at most once");
                        }
                        updatedItems.add(im);
                    } else {
                        newItems.add(im);
                    }
                });
        return ChangeSet.of(
                newItems,
                updatedItems,
                knownIM.entrySet().stream()
                        .filter(entry -> !usedIds.contains(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList())
        );
    }

}
