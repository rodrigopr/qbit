qBit - Bitmap Query Engine
=======

**Still in early stage of development**

**qBit** provides an http API on top of a bitmap query system, allowing your application to index arbitrary data and make complex query operations on it.

------

Bitmaps are commonly used as fast data structures. Unfortunately, they can use too much memory. To compensate, we often use compressed bitmaps.

qBit implements a simple sparse-bitmap algorithm, using bucket-base long arrays to be able to skip gaps on the index. 

This technique provides performance on par with conventional implementations such as [EWAH](https://github.com/lemire/javaewah) and [Concise](https://github.com/metamx/extendedset), and have the benefit 	of support 64bits values. On the bad side, it generates bigger indexes (mostly due to the choice for 64bits support).

We also favor immutability, so all operations create an entirely new index.

-----

### Few common user cases:

- You want to know which followers two users have in common
- You want to know which friends an user liked/commented on an article
- You want to filter tags of document that an user have interest
- You want to filter users of an given city that matches another criteria
- Anything that you can represent as a composition of `AND`, `OR`, `XOR`, `AND-NOT` operations.

**qBit** can solve any of those problems, providing sub-millisecond response even on indexes with millions of items.
