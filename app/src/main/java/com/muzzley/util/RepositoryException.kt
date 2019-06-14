package com.muzzley.util

class RepositoryException(cause: Throwable?, val code: Int) : Exception(cause)
