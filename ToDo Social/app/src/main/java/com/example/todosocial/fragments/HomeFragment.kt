package com.example.todosocial.fragments
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.todosocial.R
import com.example.todosocial.utils.SharedPrefsManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class HomeFragment : Fragment() {


    private val client = getUnsafeOkHttpClient()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnAddTask = view.findViewById<Button>(R.id.btnAddTask)
        val btnTaskList = view.findViewById<Button>(R.id.btnTaskList)
        val imgJoke = view.findViewById<ImageView>(R.id.imgJoke)
        val imgAdvice = view.findViewById<ImageView>(R.id.imgAdvice)

        val username = SharedPrefsManager.getUsername(requireContext())
        if (username == null) {
            findNavController().navigate(R.id.loginFragment)
        } else {
            tvWelcome.text = "Welcome, $username!"
        }

        btnLogout.setOnClickListener {
            SharedPrefsManager.logout(requireContext())
            findNavController().navigate(R.id.loginFragment)
        }

        btnAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addTaskFragment)
        }

        btnTaskList.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_taskListFragment)
        }

        imgJoke.setOnClickListener {
            makeApiCall("https://v2.jokeapi.dev/joke/Any", isJoke = true)
        }

        imgAdvice.setOnClickListener {
            makeApiCall("https://api.adviceslip.com/advice", isJoke = false)
        }

        return view
    }

    private fun makeApiCall(url: String, isJoke: Boolean) {
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    showDialog("Error", "API call failed: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                val message = try {
                    if (isJoke) {
                        val obj = JSONObject(json!!)
                        if (obj.getBoolean("error")) {
                            "Failed to get joke."
                        } else if (obj.getString("type") == "single") {
                            obj.getString("joke")
                        } else {
                            obj.getString("setup") + "\n\n" + obj.getString("delivery")
                        }
                    } else {
                        val obj = JSONObject(json!!)
                        obj.getJSONObject("slip").getString("advice")
                    }
                } catch (e: Exception) {
                    "Parsing error: ${e.message}"
                }

                requireActivity().runOnUiThread {
                    showDialog(if (isJoke) "Here's a joke" else "Advice for you", message)
                }
            }
        })
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, java.security.SecureRandom())
        }

        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }
}

