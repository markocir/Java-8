# Sorting, searching and Big O Notation
## What is Big O Notation?
Big O Notation describes how hard an algorithm has to work to solve a problem.

## Sorting and searching Algorithms

[Bubble sort](BubbleSort.java) uses less memory than Bucket sort but needs more time when looping through elements. `O(n^2)` - because it compares each element with each other element.

[Bucket sort](BucketSort.java) uses more memory than Bubble sort because its storing values into 10 separate buckets which are the same length as original array. 
After each pass all elements are stored back to the original array in a new order, this process repeats for times of the digit count of the longest number. 
`O(n*k)` where `k` is count of the digits in the longest number and `n` is count of values to be sorted.

[Quick sort](QuickSort.java) sorts a list by finding its first elements correct position (O(n)) and dividing (O(log n)) that list into two lists, one with smaller 
values than that elements value and the second with bigger values than the element. These lists are then recursively sorted again. 
This process repeats as long as there is only one element in the list. Which makes this method a Order n log n or `O(n log n)`.

[Recursive Linear Search](RecursiveLinearSearch.java) can be a lot slower than [Recursive Binary Search](RecursiveBinarySearch.java) because its comparing each element with the searched key - `O(n)`.
While the other is only comparing a necessary sorted lists middle value with the searched key - `O(log n)` because if the key is smaller, bigger values than the key are kept and vice versa. 
Then the remaining list is recursively searched until a equal value is found or there is nothing left to compare.
