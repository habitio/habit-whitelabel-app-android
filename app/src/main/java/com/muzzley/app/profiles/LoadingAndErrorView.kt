package com.muzzley.app.profiles

interface LoadingAndErrorView {
    fun showLoading(show: Boolean)
    fun showError(t: Throwable, showErrorPage: Boolean)
}
