package y9to.libs.stdlib.coroutines

import kotlinx.coroutines.Deferred
import y9to.libs.stdlib.*


suspend inline fun <A> await(a: Deferred<A>) =
    a.await()

suspend inline fun <A, B> await(a: Deferred<A>, b: Deferred<B>) =
    Tuple2(a.await(), b.await())

suspend inline fun <A, B, C> await(a: Deferred<A>, b: Deferred<B>, c: Deferred<C>) =
    Tuple3(a.await(), b.await(), c.await())

suspend inline fun <A, B, C, D> await(a: Deferred<A>, b: Deferred<B>, c: Deferred<C>, d: Deferred<D>) =
    Tuple4(a.await(), b.await(), c.await(), d.await())

suspend inline fun <A, B, C, D, E> await(a: Deferred<A>, b: Deferred<B>, c: Deferred<C>, d: Deferred<D>, e: Deferred<E>) =
    Tuple5(a.await(), b.await(), c.await(), d.await(), e.await())

suspend inline fun <A, B, C, D, E, F> await(a: Deferred<A>, b: Deferred<B>, c: Deferred<C>, d: Deferred<D>, e: Deferred<E>, f: Deferred<F>) =
    Tuple6(a.await(), b.await(), c.await(), d.await(), e.await(), f.await())

suspend inline fun <A, B, C, D, E, F, G> await(a: Deferred<A>, b: Deferred<B>, c: Deferred<C>, d: Deferred<D>, e: Deferred<E>, f: Deferred<F>, g: Deferred<G>) =
    Tuple7(a.await(), b.await(), c.await(), d.await(), e.await(), f.await(), g.await())

suspend inline fun <A, B, C, D, E, F, G, H> await(a: Deferred<A>, b: Deferred<B>, c: Deferred<C>, d: Deferred<D>, e: Deferred<E>, f: Deferred<F>, g: Deferred<G>, h: Deferred<H>) =
    Tuple8(a.await(), b.await(), c.await(), d.await(), e.await(), f.await(), g.await(), h.await())

suspend inline fun <A, B, C, D, E, F, G, H, I> await(a: Deferred<A>, b: Deferred<B>, c: Deferred<C>, d: Deferred<D>, e: Deferred<E>, f: Deferred<F>, g: Deferred<G>, h: Deferred<H>, i: Deferred<I>) =
    Tuple9(a.await(), b.await(), c.await(), d.await(), e.await(), f.await(), g.await(), h.await(), i.await())

suspend inline fun <A, B, C, D, E, F, G, H, I, J> await(a: Deferred<A>, b: Deferred<B>, c: Deferred<C>, d: Deferred<D>, e: Deferred<E>, f: Deferred<F>, g: Deferred<G>, h: Deferred<H>, i: Deferred<I>, j: Deferred<J>) =
    Tuple10(a.await(), b.await(), c.await(), d.await(), e.await(), f.await(), g.await(), h.await(), i.await(), j.await())
