package com.sos.i18n;

/** \package com.sos.i18n
 * 
 * \brief JobScheduler internationalization (i18n) Classes
 * 
 * <h1 align="center">I18N Messages and Logging</h1>
 * <p align="center">
 * <i>John Mazzitelli</i>
 * </p>
 * <p align="center">
 * <i>December 22, 2006</i>
 * </p>
 *
 * <p>
 * The purpose of this project is to provide an easy to use API that allows you
 * to incorporate internationalized (I18N) messages into your Java applications.
 * </p>
 *
 * <p>
 * This project provides an API that allows developers to:
 * </p>
 * 
 * <ul>
 * <li>Obtain I18N messages from resource bundles in any supported locale (see
 * the <a href="../api/mazz/i18n/Msg.html">Msg</a> class).</li>
 * <li>Obtain I18N messages <i>within an Ant script</i> from resource bundles in
 * any supported locale (see the <a
 * href="../api/mazz/i18n/ant/I18NMessageAntTask.html">I18NMessageAntTask</a>
 * class).</li>
 * <li>Log I18N messages using any logging framework (see the <a
 * href="../api/mazz/i18n/Logger.html">Logger</a> class and its associated <a
 * href="../api/mazz/i18n/LoggerFactory.html">LoggerFactory</a> class).</li>
 * <li>Create localized exceptions whose messages are internationalized (see <a
 * href
 * ="../api/mazz/i18n/exception/LocalizedException.html">LocalizedException</a>
 * and <a href="../api/mazz/i18n/exception/LocalizedRuntimeException.html">
 * LocalizedRuntimeException</a>)</li>
 * <li>Annotate Java classes to identify your I18N messages (see <a
 * href="../api/mazz/i18n/annotation/I18NMessage.html">@I18NMessage</a> and <a
 * href
 * ="../api/mazz/i18n/annotation/I18NResourceBundle.html">@I18NResourceBundle<
 * /a>)</li>
 * <li>Automatically generate resource bundles in any number of locales using a
 * custom Ant task (see <a
 * href="../api/mazz/i18n/ant/I18NAntTask.html">I18NAntTask</a>).</li>
 * </ul>
 * <p>
 * It is recommended that you review <a href="../api">the full API
 * documentation</a> since all classes are fully documented with Javadoc. You
 * should be able to come up to speed fairly quickly on how to use this API by
 * reading this page and the API docs.
 * </p>
 * <h2>Details</h2> <h3>Msg</h3>
 * <p>
 * The core class in the <i>i18nlog</i> project is <a
 * href="../api/mazz/i18n/Msg.html">Msg</a>. It is the object that accesses the
 * appropriate resource bundle properties file for the appropriate locale and it
 * will replace the message's placeholders (e.g. {0}, {1}) with the values
 * passed into its variable argument parameters. Although you no longer have to
 * work directly with the following core Java classes, you might want to read
 * the Javadocs on <a href=
 * "http://java.sun.com/j2se/1.5.0/docs/api/java/util/ResourceBundle.html"
 * >ResourceBundle</a> and <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/text/MessageFormat.html"
 * >MessageFormat</a> to get a feel for how <i>i18nlog</i> does what it does.
 * </p>
 * <p>
 * The <code>Msg</code> class provides a set of constructors to build your I18N
 * messages, or you can use its set of <code>createMsg</code> static methods to
 * create them. Once created, you simply call Msg::getLastMessage() or
 * ::toString() which both do the same thing - return the I18N message that was
 * last retrieved from the locale's resource bundle.
 * </p>
 * <p>
 * You can switch the locale used by the <code>Msg</code> by calling
 * ::setLocale() - calling ::getLastMessage() after doing that will return the
 * message in that locale's translation. Similarly, if you serialize a
 * <code>Msg</code> object that relies on the VM's default locale and send it
 * over the wire where the new VM's default locale is different, that
 * deserialized <code>Msg</code> object will re-translate the message in the new
 * locale and return that new localized message (of course, you will need to
 * ensure that new VM has that locale's resource bundle in the appropriate
 * classloader for the new language's message to be found).
 * </p>
 * <p>
 * The typical usage of the <code>Msg</code> class is as follows:
 * </p>
 * 
 * <pre>
 * System.out.println(Msg.createMsg(&quot;my-bundle-key&quot;, name, 5));
 * </pre>
 * <p>
 * where "my-bundle-key" refers to a message in a bundle properties file. "name"
 * and "5" is just an example of passing an arbitrary arguments list - each
 * object represents the value to replace the placeholder in its respective
 * position. For example, if "my-bundle-key" refers to a message in my bundle
 * file named "messages_en.properties" where that message is
 * "Hello {0} - you have {1} emails waiting", and "name" has the value "John",
 * then this will print out the string "Hello John - you have 5 emails waiting".
 * </p>
 * <p>
 * There are additional things you can do that won't be explained in detail
 * here; again, go to the Javadoc and review the methods available to get a
 * sense of the features that are available. Suffice it to say, in calls to
 * <code>Msg</code> methods, you can explicitly specify what locale you want the
 * messages to be in and what resource bundle to read by explicitly specifying
 * the base bundle name. In the example above, it uses the default settings
 * (e.g. the VM's default locale is used).
 * </p>
 * <p>
 * Also, you will probably want to use the @I18N-annotations in conjunction with
 * the <code>Msg</code> API (and the <code>Logger</code>/
 * <code>LocalizedException</code> API which is explained below). This would
 * enable you to have compile time checks for that "my-bundle-key" parameter
 * argument (through the use of Java constants) and it would also allow you to
 * automatically generate your resource bundles without you having to manually
 * manage/maintain them. More on this later.
 * </p>
 * <h3>Logger</h3>
 * <p>
 * <i>i18nlog</i> provides a means by which you can log I18N messages using any
 * logging framework you want. The logging frameworks <i>i18nlog</i> directly
 * supports today is Apache Log4J (<i>log4j</i>), Apache Commons Logging
 * (<i>commons-logging</i>) and JDK logging, but commons-logging itself supports
 * pluggable logging frameworks - therefore, <i>i18nlog</i> picks up that
 * capability "for free". That said, <i>i18nlog</i> was written in such a way
 * that, with a little more work, it can be made extensible to work with your
 * own logging framework directly (for those that do not want to use or have a
 * dependency on log4j or commons-logging or use JDK logging).
 * </p>
 * <p>
 * The main class of the logging subsystem is Logger. It provides the typical
 * set of <code>trace</code>, <code>debug</code>, <code>info</code>,
 * <code>warn</code>, <code>error</code> and <code>fatal</code> methods.
 * However, instead of taking a string consisting of the message itself, you
 * pass in the resource bundle key and a variable arguments list for the
 * placeholder values that are to be replaced within the message. It uses the
 * <code>Msg</code> class under the covers to get the actual localized message.
 * </p>
 * <p>
 * You obtain Logger objects by using the factory class LoggerFactory. For those
 * that have used other logging frameworks like log4j or commons-logging, the
 * programming model is basically the same when creating your Logger objects via
 * the factory:
 * </p>
 * 
 * <pre>
 * 
 * 
 * 
 * 
 * public static final Logger LOG = LoggerFactory.getLogger(MyClass.class);
 * </pre>
 * 
 * <p>
 * By default, <code>LoggerFactory</code> will use JDK logging unless Apache
 * Log4J or Apache Commons Logging is found in your classpath. If Apache Log4J
 * is found, it will be used; otherwise, if Apache Commons Logging is found, it
 * will be used (note that Log4J takes precedence over Commons). You can
 * explicitly define which logging framework to use by setting the system
 * property i18nlog.logger-type to <i>jdk</i>, <i>log4j</i> or <i>commons</i>.
 * There is also a public API, LoggerFactory.resetLoggerType(), to allow you to
 * programmatically set the logging framework to be used.
 * </p>
 * 
 * <p>
 * You can explicitly tell the factory what resource bundle the
 * <code>Logger</code> should use and what Locale the messages should be in full
 * LoggerFactory API for the methods you'd need to use to do this). Note that it
 * is possible to define a different "log locale" default that the loggers will
 * use as compared to the default locale <code>Msg</code> instances will use.
 * The LoggerLocale class is used for this feature. This is to facilitate the
 * use-case where I want to log messages in a language my support group can
 * read, but my user-interface is in a language that my users can read (which
 * may be different). For example, my users may be German-speaking, but the
 * software is supported by a group that works in France and is only
 * French-speaking. In this case, when my German users have a problem, they will
 * normally send the logs to the support group in France. Having the log
 * messages in German isn't very helpful, so the software could, by default, set
 * its log locale to Locale.FRENCH while allowing <code>Msg</code> to default to
 * the user's default locale of Locale.GERMAN. On the other hand, if my German
 * users want to try to debug a problem themselves or just wish to read the logs
 * to see what the software is doing, having the log files contain messages in
 * French isn't helpful to them. In this case, my German users will be able to
 * "flip a setting" and have the log files dump messages in German. This magical
 * setting is the locale of the LoggerLocale() - refer to its API for more
 * information on how to switch the log locale). Of course, this is all possible
 * only if I take the time to translate my resource bundle messages in both
 * French and German and I ship those two language resource bundles with the
 * software (but this is what I18N is all about so nothing should be a surprise
 * here).
 * </p>
 * <p>
 * A typical I18N logging usage is as follows:
 * </p>
 * 
 * <pre>
 *    public static final Logger LOG = LoggerFactory.getLogger(MyClass.class);
 *    ...
 *    LOG.debug("my-bundle-key", name, count);
 *    ...
 *    try
 *    {
 *       ...
 *    }
 *    catch (Exception e)
 *    {
 *       LOG.warn(e, "my-error-key", name);
 *    }
 * </pre>
 * 
 * <p>
 * The above shows two distinct logging features. The first is the fact that you
 * log an I18N message specified by the bundle key, as opposed to the actual
 * message itself. This works the same way as the <code>Msg</code> API. The
 * second feature is the logging of a message that is associated with a
 * particular exception. Notice that the exception must come as the argument
 * <i>before</i> the message key string (this is due to the nature of how
 * varargs are parsed - putting the exception last in the argument list, as is
 * the case with most other logging frameworks, would cause only the exception
 * message to be logged and not its full stack trace. This can be considered a
 * feature in and of itself; if you know you have an exception that you would
 * never want to have its stack trace dumped in the log, put it in the variable
 * arguments list so it is treated like any other object, that is, its
 * toString() value will be the only thing logged).
 * </p>
 * <p>
 * There are additional features that <i>i18nlog</i> adds to its logging
 * framework. The first is the ability to tell <code>Logger</code> not to dump
 * stack traces of exceptions even if the exception was logged using the <a
 * href=
 * "../api/mazz/i18n/Logger.html#error(java.lang.Throwable,%20java.lang.String,%20java.lang.Object...)"
 * >exception log</a> methods (there is one for each log level). This is useful
 * if you wish to run your app in a slightly quieter mode - you might not care
 * to see all the exception stack traces during a particular run. Note that this
 * feature cannot turn off stack dumps for exceptions logged at the <a href=
 * "../api/mazz/i18n/Logger.html#fatal(java.lang.Throwable,%20java.lang.String,%20java.lang.Object...)"
 * >FATAL level</a> - fatal exceptions logged with that method always have their
 * stack traces dumped. Read the API docs on the <a
 * href="../api/mazz/i18n/Logger.html">Logger</a> class to learn more about how
 * to enable and disable this feature.
 * </p>
 * <p>
 * The second additional feature in the logging framework is the ability to log
 * a message's associated resource bundle key along with the message itself. The
 * resource bundle key is the same across all locales - so no matter what
 * language the log messages are in, the keys will always be the same. This is
 * useful if a user has set his <a href="../api/mazz/i18n/LoggerLocale.html">log
 * locale</a> to a language that you cannot read and thus the log messages
 * themselves are not useful. With the resource bundle key logged along with the
 * message itself, you can use that key as the code to identify the message (you
 * could then look up that code in your software documentation or in a resource
 * bundle that contains the messages in your language). Read the API docs on the
 * <a href="../api/mazz/i18n/Logger.html">Logger</a> class to learn more about
 * how to enable and disable this feature.
 * </p>
 * 
 * <p>
 * And lastly, you can get localized messages directly from a
 * <code>Logger</code> instance via its <a href=
 * "../api/mazz/i18n/Logger.html#getMsg(java.lang.String,%20java.lang.Object...)"
 * >getMsg()</a> and <a href=
 * "../api/mazz/i18n/Logger.html#getMsgString(java.lang.String,%20java.lang.Object...)"
 * >getMsgString()</a> methods. Since <code>Logger</code> instances can be using
 * a different locale than <code>Msg</code> instances, these APIs allow you to
 * ask a <code>Logger</code> for a message in its log locale without knowing
 * what that locale is (and thus without having to tell <code>Msg</code> what to
 * use). The typical use case for this is to obtain log messages that are to be
 * passed to an exception's constructor where the exception to be instantiated
 * is not derived from <i>i18nlog</i>'s <code>LocalizedException</code> or
 * <code>LocalizedRuntimeException</code> exception classes.
 * </p>
 * 
 * <h3>Localized Exceptions</h3>
 * 
 * <p>
 * <i>i18nlog</i> provides two base exception classes (for both checked and
 * unchecked exceptions) that can be used to create your own localized
 * exceptions. See <a href="../api/mazz/i18n/exception/LocalizedException.html">
 * LocalizedException</a> and <a
 * href="../api/mazz/i18n/exception/LocalizedRuntimeException.html"
 * >LocalizedRuntimeException</a>. These have constructors whose signatures are
 * very similar to the <code>Msg</code> class. They simply allow you to specify
 * your exception message via a resource bundle key and a variable argument list
 * of placeholder values, with the ability to optionally specify the base bundle
 * name and locale. This allows your exception messages to be localized to
 * different languages, just as <code>Msg</code> can retrieve localized
 * messages.
 * </p>
 * 
 * <h3>Obtaining I18N Messages Within Ant Scripts</h3>
 * 
 * <p>
 * You can obtain I18N messages within your Ant scripts by using the <a
 * href="../api/mazz/i18n/ant/I18NMessageAntTask.html">I18N message ant task</a>
 * (named the <i>&lt;i18n-msg></i> task). This allows you to output localized
 * messages within your Ant scripts. The &lt;i18n-msg> task extends Ant's core
 * <i>&lt;echo></i> task - so all attributes that the echo task accepts are also
 * accepted by the &lt;i18n-msg> task. The only difference is the <i>message</i>
 * attribute isn't the actual message; instead it is the resource bundle message
 * key. This new Ant task can also accept a <i>bundle</i> attribute if you need
 * to define the base bundle name where your localized message is located (the
 * default is "messages"). The <i>locale</i> attribute is rarely needed, but if
 * it is set, it defines the actual locale in which your message should be
 * displayed. The default is your VM's default locale - this is normally what
 * you want. Finally, you can define a <i>property</i> attribute. If you define
 * this attribute, then this task will not echo the message; instead, the given
 * property will be set with the localized message string. If your message has
 * argument placeholders (e.g. {0}), you need to define child <i>&lt;arg></i>
 * elements to define the values for those placeholders. Here's a few examples
 * on how to use this Ant task:
 * </p>
 * 
 * <pre>
 *    &lt;taskdef name="i18n-msg"
 *             classpathref="i18nlog-jar-and-bundles.classpath"
 *             classname="mazz.i18n.ant.I18NMessageAntTask" />
 * 
 *    &lt;i18n-msg message="Example.simple-message" bundle="example-messages" />
 *    
 *    &lt;i18n-msg message="Example.simple-message" bundle="example-messages" property="test.i18n.property" />
 *    &lt;echo message="${test.i18n.property}" />
 *    
 *    &lt;i18n-msg message="Test.arg-message">
 *      &lt;arg value="100" />  
 *      &lt;arg value="2" />  
 *    &lt;i18n-msg>
 * </pre>
 * 
 * <h3>I18N Annotations and Resource Bundle Auto-Generation</h3>
 * <p>
 * So far, we've assumed that when ever you need to specify a particular
 * message, you would provide the message's bundle key by hardcoding its value
 * in the API calls; e.g.: <code>Msg.createMsg("my-bundle-key")</code>. Because
 * this does not provide a way for nice compile-time checks (to ensure that key
 * actually refers to an actual bundle message), the developer must ensure that
 * the key does not have a typo and the developer must remember to add the
 * actual message associated with that key to the resource bundle properties
 * file. This is not an easy thing to do and is ripe for problems that will not
 * manifest itself until you run your app and notice that messages are missing.
 * This isn't even easy to unit test. What we need is a way to get the compiler
 * to check these things for us. It would also be nice to have a tool that
 * automatically generates our resource bundle properties files so the developer
 * isn't responsible for manually adding new messages to them and manually
 * cleaning up old, obsolete messages that are no longer used.
 * </p>
 * <p>
 * Fortunately, <i>i18nlog</i> provides mechanisms to do those things. First,
 * there are several I18N annotations that allow you to annotate your Java
 * classes to facilitate the automatic generation of resource bundles via a
 * custom Ant task so you don't have to manually create and edit your resource
 * bundle .properties files. The message annotations are placed on constants
 * that you use in place of your resource bundle key strings, so this inherently
 * forces compile time checks. This means that typos introduced in the code
 * (i.e. misspelling a constant name) and usage of obsolete/deleted messages are
 * detected at compile time.
 * </p>
 * 
 * <p>
 * The annotations that are available are:
 * </p>
 * 
 * <ul>
 * <li><a href="../api/mazz/i18n/annotation/I18NResourceBundle.html">\@I18NResourceBundle
 * </a></li>
 * <li><a href="../api/mazz/i18n/annotation/I18NMessage.html">\@I18NMessage</a></li>
 * <li><a
 * href="../api/mazz/i18n/annotation/I18NMessages.html">\@I18NMessages</a></li>
 * </ul>
 * 
 * <p>
 * The <code>\@I18NResourceBundle</code> annotation defines what resource bundle
 * properties file you want to put your localized messages in. This can annotate
 * an entire class or interface, or it can annotate a specific field. If you
 * annotate a class or interface, all <code>\@I18NMessage</code> annotations
 * found in that class or instance will be stored, by default, in that resource
 * bundle. If the annotation is on a particular constant field, it will be the
 * default bundle for that constant only.
 * </p>
 * 
 * <p>
 * The <code>\@I18NMessage</code> can only annotate a field, and more
 * specifically, should only annotate a <code>static final</code> field that is
 * of type <code>java.lang.String</code> (in fact, an error will be generated by
 * the Ant task if this is not the case - more on the Ant task below). This
 * annotation defines the actual localized message translation for a particular
 * locale. You can define multiple message translations for a single field using
 * the <code>\@I18NMessages</code> annotation (which simply wraps multiple
 * <code>\@I18NMessage</code> annotations) The constant String value of the
 * field that you are annotating is the resource bundle key string. It is these
 * constants that you pass into the <code>Msg</code>, <code>Logger</code> and
 * <code>LocalizedException</code> methods/constructors when you want to refer
 * to a specific resource bundle key. This avoids having to hardcode string
 * literals into your method calls and thus avoids the possibility of
 * introducing a typo in the string literal and avoids the possibility that you
 * are using a message key that no longer exists (since deleting a constant
 * would cause all uses of that old constant to be flagged as an error by the
 * compiler).
 * </p>
 * 
 * <p>
 * This is probably confusing, so an example is in order here. Let's look at the
 * <a href=
 * "http://i18nlog.cvs.sourceforge.net/i18nlog/i18nlog/test/mazz/i18n/annotation/ExampleAnnotatedClass.java?view=markup"
 * >ExampleAnnotatedClass</a> that is in the <i>i18nlog</i> project as an
 * illustrative example on how to annotate your classes (this class also has a
 * couple of incorrectly used annotations; those incorrect usages are documented
 * in the comments within the class so as not to confuse the reader).
 * </p>
 * 
 * <p>
 * First, notice that the class declaration has a
 * <code>\@I18NResourceBundle</code> annotation.
 * </p>
 * 
 * <pre>
 *    \@I18NResourceBundle( baseName = "example-messages" )
 *    public class ExampleAnnotatedClass
 * </pre>
 * 
 * <p>
 * This tells us the default file where this class's I18N messages will be
 * written. In this example, all I18N messages found within the scope of this
 * class definition will be stored, by default, in the resource bundle
 * properties file named "example-messages_en_US.properties" assuming my VM's
 * default locale is "en_US". The <code>\@I18NResourceBundle</code> annotation
 * defines what the base bundle name is via its ::baseName() attribute (if you
 * do not specify the <code>baseName</code> attribute, the default will be
 * "messages"). It also indicates which locale the messages are written in via
 * the ::defaultLocale attribute (if this attribute is not specified, the
 * default will be that of the <i>defaultlocale</i> attribute of the i18n ANT
 * task or, if that is not defined, the VM's default locale). Two additional
 * things to note here: first, both <code>class</code> and
 * <code>interface</code> declarations can be annotated with
 * <code>@I18NResourceBundle</code> - since both can contain static final String
 * constants (this is useful if you want to put all of your I18N message fields
 * in a single interface - thus keeping all I18N information in a single
 * location within your code base). Secondly, this top-level annotation can be
 * overridden by placing a <code>@I18NResourceBundle</code> annotation on a
 * particular field that wants to put its messages in a resource bundle that is
 * different than that specified by the top-level annotation. In the
 * <code>ExampleAnnotatedClass</code> example, you can see this on the field
 * <code>MESSAGE_THREE_KEY</code>:
 * </p>
 * 
 * <pre>
 * 
 * 
 * 
 * 
 * &#064;I18NMessage(&quot;This is my en_CA version of the third message that should go in a en_CA bundle&quot;)
 * &#064;I18NResourceBundle(baseName = &quot;example-messages-for-third&quot;, defaultLocale = &quot;en_CA&quot;)
 * public static final String MESSAGE_THREE_KEY = &quot;Example.message3-key&quot;;
 * </pre>
 * 
 * <p>
 * Now that you have defined where you want to store your I18N messages via the
 * <code>@I18NResourceBundle</code> annotation, you then begin to define the
 * messages themselves. That's where the <code>@I18NMessage</code> and
 * <code>@I18NMessages</code> annotations come in. You need to define a static
 * final String constant whose value is the bundle key of the localized message.
 * This bundle key is the same across all locales - it identifies your message
 * regardless of what language the message is displayed as. Once you define the
 * constant, you need to annotate it to denote it as an I18N message. If you are
 * multi-lingual, you can use <code>I18NMessages</code> to provide multiple
 * translations for your message; the typically use-case, however, is that you
 * provide a single translation in a <code>@I18NMessage</code> annotation. So,
 * back to our example class, you can see these annotations at work here:
 * </p>
 * 
 * <pre>
 * 
 * 
 * 
 * 
 * &#064;I18NMessages({ @I18NMessage(&quot;This is my English message: {0}&quot;), @I18NMessage(value = &quot;This is my UK-English message: {0}&quot;, locale = &quot;en_UK&quot;),
 *         &#064;I18NMessage(value = &quot;Dieses ist meine deutsche Anzeige: {0}&quot;, locale = &quot;de&quot;) })
 * public static final String MESSAGE_KEY = &quot;Example.message-key&quot;;
 * 
 * &#064;I18NMessage(&quot;This is my English version of the second message&quot;)
 * public static final String MESSAGE_TWO_KEY = &quot;Example.message2-key&quot;;
 * </pre>
 * 
 * <p>
 * You can see that for <code>MESSAGE_KEY</code>, I decided to provide three
 * translations - one for the default locale (which is defined by my
 * <code>@I18NResourceBundle</code> annotation), one for the "en_UK" locale and
 * one for the "de" (aka German) locale. For the <code>MESSAGE_TWO_KEY</code>
 * bundle key constant, I only defined a single translation in my default
 * locale.
 * </p>
 * 
 * <p>
 * All that said, in your typical use-case, you will have a single
 * <code>@I18NResourceBundle</code> annotation on a top-level
 * <code>interface</code> and each I18N constant field will have a single
 * <code>@I18NMessage</code> that uses the default locale as defined by the
 * <code>@I18NResourceBundle</code> annotation:
 * </p>
 * 
 * <pre>
 *    @I18NResourceBundle( baseName="my-messages" defaultLocale="en" )
 *    public interface MyMessageKeys
 *    {
 *       @I18NMessage( "This is an English message" )
 *       public static final String I18N_FIRST_MESSAGE = "Example.first-message";
 * 
 *       @I18NMessage( "Hello {0}.  You visited this website {1} times" )
 *       public static final String I18N_WELCOME = "Example.welcome";
 * 
 *       // ... and any more you want to define
 *    }
 * </pre>
 * 
 * <p>
 * Now you use these in your code rather than hardcoding the string literals:
 * </p>
 * 
 * <pre>
 * LOG.debug(MyMessageKeys.I18N_FIRST_MESSAGE);
 * System.out.println(Msg.createMsg(MyMessageKeys.I18N_WELCOME, &quot;John&quot;, counter));
 * </pre>
 * 
 * <p>
 * At this point, you are still responsible for creating and maintaining your
 * own resource bundle files. In fact, as of right now, I wouldn't even need the
 * I18N annotations - simply creating constants that define my bundle keys is
 * enough to enforce compile time checks on my usage of these bundle keys.
 * However, in the above example, I would have to manually create a file called
 * "my-messages_en.properties" and edit it such that the following messages were
 * added to it:
 * </p>
 * 
 * <pre>
 *    Example.first-message=This is an English message
 *    Example.welcome=Hello {0}.  You visited this website {1} times
 * </pre>
 * 
 * <p>
 * That's where the custom <a href="../api/mazz/i18n/ant/I18NAntTask.html">I18N
 * ant task</a> comes in. This custom Ant task ships with the <i>i18nlog</i>
 * distribution. Its job is to scan your classes looking for these I18N
 * annotations and, based on them, will automatically create your resource
 * bundles for you. This means that as you add more I18N message constant
 * fields, they will automatically be added to your resource bundles. If you
 * delete an I18N message constant, that message will be removed from the
 * resulting resource bundle that the Ant task generates. Your only
 * responsibility is to maintain the accuracy of the Java constant definitions
 * and their annotations. To run the Ant task, you need to have something like
 * the following in your Ant build script:
 * </p>
 * 
 * <pre>
 *    &lt;taskdef name="i18n"
 *             classpathref="i18nlog-jar.classpath"
 *             classname="mazz.i18n.ant.I18NAntTask" />
 * 
 *    &lt;i18n outputdir="${classes.dir}"
 *          defaultlocale="en"
 *          verbose="true"
 *          verify="true">
 *       &lt;classpath refid="my.classpath" />
 *       &lt;classfileset dir="${classes.dir}"/>
 *    &lt;/i18n>
 * </pre>
 * 
 * <p>
 * If you are building your projects using Maven2, you can embed this Ant task
 * in your Maven pom.xml using something like this:
 * </p>
 * 
 * <pre>
 *   &lt;build>    
 *     &lt;plugins>
 *       &lt;plugin>
 *         &lt;artifactId>maven-antrun-plugin&lt;/artifactId>
 *         &lt;executions>
 *           &lt;execution>
 *             &lt;phase>process-classes&lt;/phase>
 *             &lt;configuration>
 *               &lt;tasks>
 *                 &lt;!-- generate the I18N resource bundles --&gt;
 *                 &lt;taskdef name="i18n"
 *                          classpathref="maven.runtime.classpath"
 *                          classname="mazz.i18n.ant.I18NAntTask" />
 * 
 *                 &lt;i18n outputdir="${project.build.outputDirectory}"
 *                       defaultlocale="en"
 *                       verbose="false"
 *                       verify="true">
 *                    &lt;classpath refid="maven.runtime.classpath" />
 *                    &lt;classfileset dir="${project.build.outputDirectory}">
 *                       &lt;include name="**&#47;*.class"/>
 *                    &lt;/classfileset>
 *                 &lt;/i18n>
 *               &lt;/tasks>
 *             &lt;/configuration>
 *             &lt;goals>
 *               &lt;goal>run&lt;/goal>
 *             &lt;/goals>
 *           &lt;/execution>
 *         &lt;/executions>
 *       &lt;/plugin>
 *     &lt;/plugins>  
 *   &lt;/build>
 * </pre>
 * 
 * <p>
 * The only things you really have to worry about here is that 1) you must give
 * the Ant task a classpath that can find your I18N annotated classes and their
 * dependencies (&lt;classpath&gt;) and 2) you have to give a set of class files
 * to the Ant task which is the list of files that are to be scanned for I18N
 * annotations (&lt;classfileset&gt;). It is recommended that you use the
 * <code>verbose</code> mode the first time you use the Ant task so you can see
 * what its doing. Once you get the build the way you want it, you can turn off
 * verbose mode.
 * </p>
 * <h4>Generating Help Documentation</h4>
 * <p>
 * One optional feature you can use with this ANT task is the ability to
 * generate help documentation which consists of a reference of all your
 * resource bundle key names with their messages along with some additional
 * description of what the message means. There is an optional attribute you can
 * specify in your @I18NMessage annotations - the <i>help</i> attribute. Its
 * value can be any string that further describes the message. Think of this as
 * documentation that further describes what situation occurred that the message
 * is trying to convey. The auto-generated help documents can, therefore,
 * provide a cross-reference between the message keys, the messages themselves
 * and more helpful descriptions of the messages.
 * </p>
 * <p>
 * Many times, you can use this as a "message code" or "error code" listing,
 * where each of your resource bundle keys can be considered a "message code" or
 * "error code". To generate help documentation, you need to use the
 * &lt;helpdoc> inner tag inside of the &lt;i18n> task:
 * </p>
 * 
 * <pre>
 *    &lt;i18n outputdir="${classes.dir}">
 *       &lt;classpath refid="my.classpath" />
 *       &lt;classfileset dir="${classes.dir}"/>
 *       &lt;helpdoc outputdir="${doc.dir}/help"/>
 *    &lt;/i18n>
 * </pre>
 * 
 * <p>
 * For every resource bundle generated, you will get an additional help document
 * output in the given directory specified in &lt;helpdoc>. There are additional
 * attributes you can specify in the &lt;helpdoc> tag:
 * </p>
 * 
 * <ul>
 * <li><b>append</b>: can be <i>true</i> or <i>false</i>. If <i>true</i>, any
 * existing help document files are appended to; default is <i>false</i>.</li>
 * <li><b>outputfileext</b>: each output help document's filename will be the
 * resource bundle base name followed by the locale string followed by a file
 * extension, whose default is <i>.html</i>. An example is
 * <i>mymessages_en_US.html</i>. If you wish to change that extension string,
 * you do so by setting this attribute.</li>
 * <li><b>helpmessagesonly</b>: can be <i>true</i> or <i>false</i>. If
 * <i>false</i> (which is the default), then all messages will have a help doc
 * item generated for it. If <i>true</i>, only those messages that have a help
 * description string specified in their I18NMessage annotations will have help
 * doc items generated for them - all others will be ignored.</li>
 * <li><b>templateitem</b>: each message in each bundle will have a help
 * document item written for it. The template item is a file that will contain a
 * template string that has replacement strings in it that will be replaced with
 * text appropriate for each I18N message. The template string will then be
 * written to its appropriate output help document file.</li>
 * <li><b>templateheader</b>: a file containing a template for the beginning of
 * each help document.</li>
 * <li><b>templatefooter</b>: a file containing a template for the end of each
 * help document.</li>
 * <li><b>templatecharset</b>: if the template files' contents all use a
 * character set that is different than your Java VM's default charset, set
 * their charset identifier via this attribute.</li>
 * </ul>
 * 
 * <p>
 * More needs to be said about the <i>templateitem</i> attribute. When
 * specified, this must point to a file that contains a string that will be
 * copied for each message. The contents in the template file is a template for
 * each message's help item. The template can contain one or more replacement
 * strings:
 * </p>
 * 
 * <ul>
 * <li>@@@I18NBUNDLE@@@: replaced with the bundle base name where the message
 * exists</li>
 * <li>@@@I18NKEY@@@: replaced with the message's resource bundle key (aka the
 * "message code" or "error code")</li>
 * <li>@@@I18NMESSAGE@@@: replaced with the message's actual text value (i.e.
 * the text string associated with the resource bundle key)</li>
 * <li>@@@I18NHELP@@@: replaced with the message's help description (i.e. the
 * help attribute of the @I18NMessage annotation).</li>
 * </ul>
 * 
 * <p>
 * For each message, the template item string will be written to the associated
 * help document - with each replacement string in the template item string
 * replaced according to the message. For example, if the message belongs to
 * bundle "mymessages_en" and has a key of "my.bundle.key" with a value of
 * "This is my message {0}", the template item string will be written to the
 * help document "mymessages_en.html" with all @@@I18NBUNDLE@@@ replacement
 * strings replaced with "mymessages_en", all @@@I18NKEY@@@ replacement strings
 * replaced with "my.bundle.key" and all @@@I18NMESSAGE@@@ replacement strings
 * replaced with "This is my message {0}". If the I18NMessage had a help
 * attribute specified, its value will replace the @@@I18NHELP@@@ string. If no
 * template item file is specified, a default template string will be used that
 * will generate a simple HTML table.
 * </p>
 * 
 * <p>
 * Note that these replacement strings are only meaningful within the contents
 * of the template item file with the exception of @@@I18NBUNDLE@@@ which is
 * also replaced within the contents of the template header and footer files.
 * </p>
 * 
 * <p>
 * The idea behind this help documentation generation is that you will end up
 * with a document (or documents) containing a list of all your message key
 * codes and their messages. You can then edit those help documents by providing
 * additional documentation on what those messages mean.
 * </p>
 * 
 * <hr/>
 * <h3>Building From Source</h3>
 * 
 * <p>
 * First, grab the code from the CVS repository <a
 * href="http://sourceforge.net/cvs/?group_id=169460">as explained here</a>. The
 * module name is "i18nlog":
 * </p>
 * 
 * <pre>
 *    cvs -z3 -d:pserver:anonymous@i18nlog.cvs.sourceforge.net:/cvsroot/i18nlog co -P i18nlog
 * </pre>
 * 
 * <p>
 * Now, all you need to do is run the <a href=
 * "http://i18nlog.cvs.sourceforge.net/i18nlog/i18nlog/build.xml?view=markup"
 * >Ant build script</a> to build the distribution (note that this requires a
 * JDK that is at version 1.5 or higher):
 * </p>
 * 
 * <pre>
 *    ant package-dist
 * </pre>
 * 
 * <p>
 * If you wish to see the custom Ant task generate an example set of resource
 * bundles, execute the <a href=
 * "http://i18nlog.cvs.sourceforge.net/i18nlog/i18nlog/i18n-test-build.xml?view=markup"
 * >sample test script</a> like this:
 * </p>
 * 
 * <pre>
 *    ant -f i18n-test-build.xml
 * </pre>
 * 
 * <p>
 * You can then compare the <a href=
 * "http://i18nlog.cvs.sourceforge.net/i18nlog/i18nlog/test/mazz/i18n/annotation/ExampleAnnotatedClass.java?view=markup"
 * >ExampleAnnotatedTestClass</a> and its annotations with the resource bundles
 * that were generated and stored in the <code>build/test-ant-output</code>
 * directory.
 * </p>
 * 
 * <p align="center">
 * <i>Copyright &#169; 2006 John J. Mazzitelli All Rights Reserved.</i>
 * </p> */

